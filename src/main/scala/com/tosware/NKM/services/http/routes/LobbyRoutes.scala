package com.tosware.NKM.services.http.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import com.tosware.NKM.models.JwtContent
import com.tosware.NKM.models.lobby._
import com.tosware.NKM.services.LobbyService
import com.tosware.NKM.services.http.directives.JwtDirective
import enumeratum.values.{StringEnum, StringEnumEntry}
import spray.json._

sealed abstract class LobbyRoute(val value: String) extends StringEnumEntry

object LobbyRoute extends StringEnum[LobbyRoute] {
  val values = findValues
  case object Lobbies extends LobbyRoute("lobbies")
  case object Lobby extends LobbyRoute("lobby")
  case object CreateLobby extends LobbyRoute("create_lobby")
  case object JoinLobby extends LobbyRoute("join_lobby")
  case object LeaveLobby extends LobbyRoute("leave_lobby")
  case object SetHexMap extends LobbyRoute("set_hexmap")
  case object SetPickType extends LobbyRoute("set_pick_type")
  case object SetNumberOfBans extends LobbyRoute("set_number_of_bans")
  case object SetNumberOfCharacters extends LobbyRoute("set_number_of_characters")
  case object StartGame extends LobbyRoute("start_game")
}



trait LobbyRoutes extends JwtDirective
  with SprayJsonSupport
{
  implicit val lobbyService: LobbyService

  val lobbyGetRoutes = concat(
    path(LobbyRoute.Lobby.value) {
      val lobbies = lobbyService.getAllLobbies()
      complete(lobbies)
    },
    path(LobbyRoute.Lobby.value / Segment) { (lobbyId: String) =>
      val lobby = lobbyService.getLobby(lobbyId)
      complete(lobby)
    },
  )

  val lobbyPostRoutes = concat(
    path(LobbyRoute.CreateLobby.value) {
      authenticated { jwtClaim =>
        entity(as[LobbyCreationRequest]) { entity =>
          val username = jwtClaim.content.parseJson.convertTo[JwtContent].content
          lobbyService.createLobby(entity.name, username) match {
            case LobbyService.LobbyCreated(lobbyId) => complete(StatusCodes.Created, lobbyId)
            case LobbyService.LobbyCreationFailure => complete(StatusCodes.InternalServerError)
            case _ => complete(StatusCodes.InternalServerError)
          }
        }
      }
    },

    path(LobbyRoute.JoinLobby.value) {
      authenticated { jwtClaim =>
        entity(as[LobbyJoinRequest]) { entity =>
          val username = jwtClaim.content.parseJson.convertTo[JwtContent].content
          lobbyService.joinLobby(username, entity) match {
            case LobbyService.Success => complete(StatusCodes.OK)
            case LobbyService.Failure => complete(StatusCodes.InternalServerError)
            case _ => complete(StatusCodes.InternalServerError)
          }
        }
      }
    },

    path(LobbyRoute.LeaveLobby.value) {
      authenticated { jwtClaim =>
        entity(as[LobbyLeaveRequest]) { entity =>
          val username = jwtClaim.content.parseJson.convertTo[JwtContent].content
          lobbyService.leaveLobby(username, entity) match {
            case LobbyService.Success => complete(StatusCodes.OK)
            case LobbyService.Failure => complete(StatusCodes.InternalServerError)
            case _ => complete(StatusCodes.InternalServerError)
          }
        }
      }
    },

    path(LobbyRoute.SetHexMap.value) {
      authenticated { jwtClaim =>
        entity(as[SetHexMapNameRequest]) { entity =>
          val username = jwtClaim.content.parseJson.convertTo[JwtContent].content
          lobbyService.setHexmapName(username, entity) match {
            case LobbyService.Success => complete(StatusCodes.OK)
            case LobbyService.Failure => complete(StatusCodes.InternalServerError)
            case _ => complete(StatusCodes.InternalServerError)
          }
        }
      }
    },

    path(LobbyRoute.SetPickType.value) {
      authenticated { jwtClaim =>
        entity(as[SetPickTypeRequest]) { request =>
          val username = jwtClaim.content.parseJson.convertTo[JwtContent].content
          lobbyService.setPickType(username, request) match {
            case LobbyService.Success => complete(StatusCodes.OK)
            case LobbyService.Failure => complete(StatusCodes.InternalServerError)
            case _ => complete(StatusCodes.InternalServerError)
          }
        }
      }
    },

    path(LobbyRoute.SetNumberOfBans.value) {
      authenticated { jwtClaim =>
        entity(as[SetNumberOfBansRequest]) { request =>
          val username = jwtClaim.content.parseJson.convertTo[JwtContent].content
          lobbyService.setNumberOfBans(username, request) match {
            case LobbyService.Success => complete(StatusCodes.OK)
            case LobbyService.Failure => complete(StatusCodes.InternalServerError)
            case _ => complete(StatusCodes.InternalServerError)
          }
        }
      }
    },

    path(LobbyRoute.SetNumberOfCharacters.value) {
      authenticated { jwtClaim =>
        entity(as[SetNumberOfCharactersPerPlayerRequest]) { request =>
          val username = jwtClaim.content.parseJson.convertTo[JwtContent].content
          lobbyService.setNumberOfCharactersPerPlayer(username, request) match {
            case LobbyService.Success => complete(StatusCodes.OK)
            case LobbyService.Failure => complete(StatusCodes.InternalServerError)
            case _ => complete(StatusCodes.InternalServerError)
          }
        }
      }
    },
    path(LobbyRoute.StartGame.value) {
      authenticated { jwtClaim =>
        entity(as[StartGameRequest]) { entity =>
          val username = jwtClaim.content.parseJson.convertTo[JwtContent].content
          lobbyService.startGame(username, entity) match {
            case LobbyService.Success => complete(StatusCodes.OK)
            case LobbyService.Failure => complete(StatusCodes.InternalServerError)
            case _ => complete(StatusCodes.InternalServerError)
          }
        }
      }
    },
  )
}
