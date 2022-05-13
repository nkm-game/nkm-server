package com.tosware.NKM.services.http.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import com.tosware.NKM.services.GameService
import com.tosware.NKM.services.http.directives.JwtDirective

trait GameRoutes extends JwtDirective
  with SprayJsonSupport
{
  implicit val gameService: GameService

  val gameGetRoutes = concat(
    path("state"/ Segment) { (gameId: String) =>
      complete(gameService.getGameState(gameId))
    },
  )

//  val gamePostRoutes = concat(
//    path("place_character") {
//      authenticated { username =>
//        entity(as[PlaceCharacterRequest]) { entity =>
//          onSuccess(gameService.placeCharacter(username, entity)) {
//            case CommandResponse.Success => complete(StatusCodes.OK)
//            case CommandResponse.Failure => complete(StatusCodes.InternalServerError)
//          }
//        }
//      }
//    },
//  )
}
