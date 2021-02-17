package com.tosware.NKM.services

import java.security.{KeyStore, SecureRandom}
import java.time.Instant

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.{ConnectionContext, HttpsConnectionContext}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{entity, _}
import akka.http.scaladsl.server.{Directive1, Route}
import akka.pattern.ask
import akka.util.Timeout
import com.tosware.NKM.CORSHandler
import com.tosware.NKM.Main.getClass
import com.tosware.NKM.serializers.NKMJsonProtocol
import com.tosware.NKM.services.UserService.{InvalidCredentials, LoggedIn}
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim, JwtSprayJson}
import spray.json._
import com.tosware.NKM.actors.Game._
import com.tosware.NKM.actors.NKMData.GetHexMaps
import com.tosware.NKM.actors.User.{RegisterFailure, RegisterSuccess}
import com.tosware.NKM.actors._
import com.tosware.NKM.models._
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

import scala.util.{Failure, Success}
import scala.concurrent.duration._
import scala.language.postfixOps

trait HttpService extends NKMJsonProtocol with SprayJsonSupport with CORSHandler {
  implicit val system: ActorSystem
  implicit val timeout: Timeout = Timeout(2 seconds)
  lazy val nkmData: ActorRef = system.actorOf(NKMData.props())

  val jwtSecretKey = "much_secret"

  def authenticated: Directive1[JwtClaim] =
    optionalHeaderValueByName("Authorization").flatMap {
      case Some(bearerToken) =>
        val token = bearerToken.split(' ')(1)
        JwtSprayJson.decode(token, jwtSecretKey, Seq(JwtAlgorithm.HS256)) match {
          case Success(value) => provide(value)
          case Failure(exception) => complete(StatusCodes.Unauthorized, exception.getMessage)
        }
      case _ => complete(StatusCodes.Unauthorized)
    }

  def getToken(login: String): String = {
    val claim: JwtClaim = JwtClaim(
      content = JwtContent(login).toJson.toString,
      expiration = Some(Instant.now.plusSeconds(157784760).getEpochSecond),
      issuedAt = Some(Instant.now.getEpochSecond)
    )
    val token = Jwt.encode(claim, jwtSecretKey, JwtAlgorithm.HS256)
    token
  }

  def getHttps: HttpsConnectionContext = {
    val password = "password".toCharArray //TODO: do not store passwords in code, read them from somewhere safe!
    val ks: KeyStore = KeyStore.getInstance("PKCS12")
    val keystore = getClass.getClassLoader.getResourceAsStream("mykeystore.pkcs12")

    require(keystore != null, "Keystore required!")
    ks.load(keystore, password)

    val keyManagerFactory = KeyManagerFactory.getInstance("SunX509")
    keyManagerFactory.init(ks, password)

    val tmf = TrustManagerFactory.getInstance("SunX509")
    tmf.init(ks)

    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(keyManagerFactory.getKeyManagers, tmf.getTrustManagers, new SecureRandom)
    val https = ConnectionContext.httpsServer(sslContext)
    https
  }


  val routes: Route =
    corsHandler {
      pathPrefix("api") {
        get {
          path("state"/ Segment) { (gameId: String) =>
            complete((system.actorOf(Game.props(gameId)) ? GetState).mapTo[GameState])
          } ~
            path("maps") {
              complete((nkmData ? GetHexMaps).mapTo[List[HexMap]])
            } ~
            path("secret") {
              authenticated { jwtClaim =>
                complete(jwtClaim.content)
              }
            }
        } ~
        post {
          path("register") {
            entity(as[RegisterRequest]) { entity =>
              println(s"Received register request for ${entity.login}")
              UserService.register(entity) match {
                case RegisterSuccess => complete(StatusCodes.Created)
                case RegisterFailure => complete(StatusCodes.Conflict) // TODO - change status code based on failure
              }
            }
          }
        } ~
        post {
          path("login") {
              entity(as[Credentials]) { entity =>
                println(s"Logging in ${entity.login}")
                UserService.authenticate(entity) match {
                  case LoggedIn(login) => complete(StatusCodes.OK, getToken(login))
                  case InvalidCredentials => complete(StatusCodes.Unauthorized, "invalid credentials")
                }
              }
            }
          }
      }
    }

}
