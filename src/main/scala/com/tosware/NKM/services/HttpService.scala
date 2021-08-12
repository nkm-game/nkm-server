package com.tosware.NKM.services

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{entity, _}
import akka.http.scaladsl.server.{Directive1, Route}
import akka.http.scaladsl.{ConnectionContext, HttpsConnectionContext}
import akka.pattern.ask
import com.tosware.NKM.actors.Game._
import com.tosware.NKM.actors.NKMData.GetHexMaps
import com.tosware.NKM.actors.User.{RegisterFailure, RegisterSuccess}
import com.tosware.NKM.actors._
import com.tosware.NKM.models._
import com.tosware.NKM.models.game.{GameState, HexMap}
import com.tosware.NKM.models.lobby._
import com.tosware.NKM.serializers.NKMJsonProtocol
import com.tosware.NKM.services.UserService.{InvalidCredentials, LoggedIn}
import com.tosware.NKM.{CORSHandler, NKMTimeouts}
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim, JwtSprayJson}
import pl.iterators.kebs.json.KebsEnumFormats
import spray.json._

import java.security.{KeyStore, SecureRandom}
import java.time.Instant
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}
import scala.language.postfixOps
import scala.util.{Failure, Success}

trait HttpService
    extends CORSHandler
    with SprayJsonSupport
    with NKMJsonProtocol
    with NKMTimeouts
{
  implicit val system: ActorSystem
  implicit val userService: UserService
  implicit val lobbyService: LobbyService
  implicit val nkmDataService: NKMDataService
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
          concat(
            path("state"/ Segment) { (gameId: String) =>
              complete((system.actorOf(Game.props(gameId)) ? GetState).mapTo[GameState])
            },
            path("maps") {
              complete(nkmDataService.getHexMaps)
            },
            path("characters") {
              complete(nkmDataService.getCharactersMetadata)
            },
            path("secret") {
              authenticated { jwtClaim =>
                complete(jwtClaim.content)
              }
            },
            path("lobbies") {
              val lobbies = lobbyService.getAllLobbies()
              complete(lobbies)
            },
            path("lobby" / Segment) { (lobbyId: String) =>
              val lobby = lobbyService.getLobby(lobbyId)
              complete(lobby)
            },
          )
        } ~
        post {
          concat(
            path("register") {
              entity(as[RegisterRequest]) { entity =>
                println(s"Received register request for ${entity.login}")
                userService.register(entity) match {
                  case RegisterSuccess => complete(StatusCodes.Created)
                  case RegisterFailure => complete(StatusCodes.Conflict) // TODO - change status code based on failure
                }
              }
            },
            path("login") {
              entity(as[Credentials]) { entity =>
                println(s"Logging in ${entity.login}")
                userService.authenticate(entity) match {
                  case LoggedIn(login) => complete(StatusCodes.OK, getToken(login))
                  case InvalidCredentials => complete(StatusCodes.Unauthorized, "invalid credentials")
                }
              }
            },
            path("create_lobby") {
              authenticated { jwtClaim =>
                entity(as[LobbyCreationRequest]) { entity =>
                  import LobbyService._
                  val username = jwtClaim.content.parseJson.convertTo[JwtContent].content
                  lobbyService.createLobby(entity.name, username) match {
                    case LobbyCreated(lobbyId) => complete(StatusCodes.Created, lobbyId)
                    case LobbyCreationFailure => complete(StatusCodes.InternalServerError)
                  }
                }
              }
            },

            path("join_lobby") {
              authenticated { jwtClaim =>
                entity(as[LobbyJoinRequest]) { entity =>
                  val username = jwtClaim.content.parseJson.convertTo[JwtContent].content
                  lobbyService.joinLobby(username, entity) match {
                    case LobbyService.Success => complete(StatusCodes.OK)
                    case LobbyService.Failure => complete(StatusCodes.InternalServerError)
                  }
                }
              }
            },

            path("leave_lobby") {
              authenticated { jwtClaim =>
                entity(as[LobbyLeaveRequest]) { entity =>
                  val username = jwtClaim.content.parseJson.convertTo[JwtContent].content
                  lobbyService.leaveLobby(username, entity) match {
                    case LobbyService.Success => complete(StatusCodes.OK)
                    case LobbyService.Failure => complete(StatusCodes.InternalServerError)
                  }
                }
              }
            },

            path("set_hexmap") {
              authenticated { jwtClaim =>
                entity(as[SetHexMapNameRequest]) { entity =>
                  val username = jwtClaim.content.parseJson.convertTo[JwtContent].content
                  lobbyService.setHexmapName(username, entity) match {
                    case LobbyService.Success => complete(StatusCodes.OK)
                    case LobbyService.Failure => complete(StatusCodes.InternalServerError)
                  }
                }
              }
            },

            path("set_pick_type") {
              authenticated { jwtClaim =>
                entity(as[SetPickTypeRequest]) { request =>
                  val username = jwtClaim.content.parseJson.convertTo[JwtContent].content
                  lobbyService.setPickType(username, request) match {
                    case LobbyService.Success => complete(StatusCodes.OK)
                    case LobbyService.Failure => complete(StatusCodes.InternalServerError)
                  }
                }
              }
            },

            path("set_number_of_bans") {
              authenticated { jwtClaim =>
                entity(as[SetNumberOfBansRequest]) { request =>
                  val username = jwtClaim.content.parseJson.convertTo[JwtContent].content
                  lobbyService.setNumberOfBans(username, request) match {
                    case LobbyService.Success => complete(StatusCodes.OK)
                    case LobbyService.Failure => complete(StatusCodes.InternalServerError)
                  }
                }
              }
            },

            path("set_number_of_characters") {
              authenticated { jwtClaim =>
                entity(as[SetNumberOfCharactersPerPlayerRequest]) { request =>
                  val username = jwtClaim.content.parseJson.convertTo[JwtContent].content
                  lobbyService.setNumberOfCharactersPerPlayer(username, request) match {
                    case LobbyService.Success => complete(StatusCodes.OK)
                    case LobbyService.Failure => complete(StatusCodes.InternalServerError)
                  }
                }
              }
            },

            path("start_game") {
              authenticated { jwtClaim =>
                entity(as[StartGameRequest]) { entity =>
                  val username = jwtClaim.content.parseJson.convertTo[JwtContent].content
                  lobbyService.startGame(username, entity) match {
                    case LobbyService.Success => complete(StatusCodes.OK)
                    case LobbyService.Failure => complete(StatusCodes.InternalServerError)
                  }
                }
              }
            },
          )
        }
      }
    }

}
