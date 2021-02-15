package com.tosware.NKM

import java.security.{KeyStore, SecureRandom}
import java.time.Instant

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directive1, Route}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.{ConnectionContext, Http}
import akka.pattern.ask
import akka.util.Timeout
import com.tosware.NKM.actors.Game._
import com.tosware.NKM.actors.NKMData.GetHexMaps
import com.tosware.NKM.actors._
import com.tosware.NKM.models._
import com.tosware.NKM.serializers.NKMJsonProtocol
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim, JwtSprayJson}
import spray.json._

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

trait Service extends NKMJsonProtocol with SprayJsonSupport with CORSHandler {
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
          path("login") {
            entity(as[Login]) { entity =>
              println(s"Logging in ${entity.login}")
              if (entity.login == "tojatos" && entity.password == "password") {
                val claim = JwtClaim(
                  content = JwtContent(entity.login).toJson.toString,
                  expiration = Some(Instant.now.plusSeconds(157784760).getEpochSecond),
                  issuedAt = Some(Instant.now.getEpochSecond)
                )
                val token = Jwt.encode(claim, jwtSecretKey, JwtAlgorithm.HS256)
                complete(StatusCodes.OK, token)
              } else {
                complete(StatusCodes.Unauthorized, "invalid credentials")
              }
            }
          }
        }
      }
    }

}

object Main extends App with Service {
  override implicit val system: ActorSystem = ActorSystem("NKMServer")

  def getHttps = {
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

  sys.env.getOrElse("DEBUG", "false").toBooleanOption match {
    case Some(true) =>
      Http().newServerAt("0.0.0.0", 8080).bind(routes)
      println("Started http server")
    case _ =>
      Http().newServerAt("0.0.0.0", 8080).enableHttps(getHttps).bindFlow(routes)
      println("Started https server")
  }


}
