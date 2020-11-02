package com.tosware.NKM

import java.security.{KeyStore, SecureRandom}
import java.util.UUID.randomUUID

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
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
import pdi.jwt.{Jwt, JwtAlgorithm}
import spray.json._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

object Main extends App with NKMJsonProtocol with SprayJsonSupport with CORSHandler {

  implicit val system: ActorSystem = ActorSystem("NKMServer")
  implicit val timeout: Timeout = Timeout(2 seconds)

  val nkmData = system.actorOf(NKMData.props())


  def startServer() = {
    val password = "password".toCharArray // do not store passwords in code, read them from somewhere safe!
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

    val jwtSecretKey = "much_secret"

    val skeleton =
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
              entity(as[String]) { token =>
                Jwt.decodeRawAll(token.trim(), jwtSecretKey, Seq(JwtAlgorithm.HS256)) match {
                  case Success(value) => complete(value._2.parseJson.convertTo[Map[String, String]].get("user").head)
                  case Failure(exception) => complete(StatusCodes.Unauthorized, exception.getMessage)
                }
              }
            }
          } ~
          post {
            path("login") {
              entity(as[Login]) { entity =>
                println(s"Logging in ${entity.login}")
                if (entity.login == "tojatos" && entity.password == "password") {
                  val token = Jwt.encode("""{"user":"tojatos"}""", jwtSecretKey, JwtAlgorithm.HS256)
                  complete(StatusCodes.OK, token)
                } else {
                  complete(StatusCodes.Unauthorized, "invalid credentials")
                }
              }
            }
          }
        }
      }
    Http().newServerAt("0.0.0.0", 8080).enableHttps(https).bindFlow(skeleton)
  }

  def test(): Unit = {
    val hexMaps = Await.result((nkmData ? GetHexMaps).mapTo[List[HexMap]], 2 seconds)
    val game = system.actorOf(Game.props("1"))

    val playerNames = List("Ryszard", "Ania", "Ola")
    val characters: List[NKMCharacter] = List[NKMCharacter](
      NKMCharacter(randomUUID().toString, "Aqua", 12, Stat(32), Stat(43), Stat(4), Stat(34), Stat(4)),
      NKMCharacter(randomUUID().toString, "Dekomori Sanae", 14, Stat(32), Stat(43), Stat(4), Stat(34), Stat(4)),
      NKMCharacter(randomUUID().toString, "Touka", 0, Stat(34), Stat(43), Stat(4), Stat(34), Stat(5))
    )

    val touka = characters.find(_.name == "Touka").get

    playerNames.foreach(n => game ! AddPlayer(n))
    //    val players = Await.result((game ? GetState).mapTo[GameState].map(s => s.players), 2 seconds)

    characters.foreach(c => game ! AddCharacter("Ola", c))

    game ! SetMap(hexMaps.head)
    game ! PlaceCharacter(HexCoordinates(4, 5), touka.id)
    game ! MoveCharacter(HexCoordinates(0, 0), touka.id)
  }

  //  test()
  startServer()
}
