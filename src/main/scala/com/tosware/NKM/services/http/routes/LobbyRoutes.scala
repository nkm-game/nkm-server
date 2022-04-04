package com.tosware.NKM.services.http.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import com.tosware.NKM.models.lobby.ws._
import com.tosware.NKM.services.LobbyService
import com.tosware.NKM.services.http.directives.JwtDirective


trait LobbyRoutes extends JwtDirective
  with SprayJsonSupport
{
  implicit val lobbyService: LobbyService

  val lobbyGetRoutes = concat(
    path(LobbyRoute.Lobbies.value) {
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
      authenticated { username =>
        entity(as[LobbyCreationRequest]) { entity =>
          lobbyService.createLobby(entity.name, username) match {
            case LobbyService.LobbyCreated(lobbyId) => complete(StatusCodes.Created, lobbyId)
            case LobbyService.LobbyCreationFailure => complete(StatusCodes.InternalServerError)
            case _ => complete(StatusCodes.InternalServerError)
          }
        }
      }
    },

    path(LobbyRoute.JoinLobby.value) {
      authenticated { username =>
        entity(as[LobbyJoinRequest]) { entity =>
          lobbyService.joinLobby(username, entity) match {
            case LobbyService.Success => complete(StatusCodes.OK)
            case LobbyService.Failure => complete(StatusCodes.InternalServerError)
            case _ => complete(StatusCodes.InternalServerError)
          }
        }
      }
    },

    path(LobbyRoute.LeaveLobby.value) {
      authenticated { username =>
        entity(as[LobbyLeaveRequest]) { entity =>
          lobbyService.leaveLobby(username, entity) match {
            case LobbyService.Success => complete(StatusCodes.OK)
            case LobbyService.Failure => complete(StatusCodes.InternalServerError)
            case _ => complete(StatusCodes.InternalServerError)
          }
        }
      }
    },

    path(LobbyRoute.SetHexMap.value) {
      authenticated { username =>
        entity(as[SetHexMapNameRequest]) { entity =>
          lobbyService.setHexmapName(username, entity) match {
            case LobbyService.Success => complete(StatusCodes.OK)
            case LobbyService.Failure => complete(StatusCodes.InternalServerError)
            case _ => complete(StatusCodes.InternalServerError)
          }
        }
      }
    },

    path(LobbyRoute.SetPickType.value) {
      authenticated { username =>
        entity(as[SetPickTypeRequest]) { request =>
          lobbyService.setPickType(username, request) match {
            case LobbyService.Success => complete(StatusCodes.OK)
            case LobbyService.Failure => complete(StatusCodes.InternalServerError)
            case _ => complete(StatusCodes.InternalServerError)
          }
        }
      }
    },

    path(LobbyRoute.SetNumberOfBans.value) {
      authenticated { username =>
        entity(as[SetNumberOfBansRequest]) { request =>
          lobbyService.setNumberOfBans(username, request) match {
            case LobbyService.Success => complete(StatusCodes.OK)
            case LobbyService.Failure => complete(StatusCodes.InternalServerError)
            case _ => complete(StatusCodes.InternalServerError)
          }
        }
      }
    },

    path(LobbyRoute.SetNumberOfCharacters.value) {
      authenticated { username =>
        entity(as[SetNumberOfCharactersPerPlayerRequest]) { request =>
          lobbyService.setNumberOfCharactersPerPlayer(username, request) match {
            case LobbyService.Success => complete(StatusCodes.OK)
            case LobbyService.Failure => complete(StatusCodes.InternalServerError)
            case _ => complete(StatusCodes.InternalServerError)
          }
        }
      }
    },
    path(LobbyRoute.StartGame.value) {
      authenticated { username =>
        entity(as[StartGameRequest]) { entity =>
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
