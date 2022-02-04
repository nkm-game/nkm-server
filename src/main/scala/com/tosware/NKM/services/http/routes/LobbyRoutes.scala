package com.tosware.NKM.services.http.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import com.tosware.NKM.models.JwtContent
import com.tosware.NKM.models.lobby._
import com.tosware.NKM.services.LobbyService
import com.tosware.NKM.services.http.directives.JwtDirective
import spray.json._

trait LobbyRoutes extends JwtDirective
  with SprayJsonSupport
{
  implicit val lobbyService: LobbyService

  val lobbyGetRoutes = concat(
    path("lobbies") {
      val lobbies = lobbyService.getAllLobbies()
      complete(lobbies)
    },
    path("lobby" / Segment) { (lobbyId: String) =>
      val lobby = lobbyService.getLobby(lobbyId)
      complete(lobby)
    },
  )

  val lobbyPostRoutes = concat(
    path("create_lobby") {
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

    path("join_lobby") {
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

    path("leave_lobby") {
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

    path("set_hexmap") {
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

    path("set_pick_type") {
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

    path("set_number_of_bans") {
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

    path("set_number_of_characters") {
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
    path("start_game") {
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
