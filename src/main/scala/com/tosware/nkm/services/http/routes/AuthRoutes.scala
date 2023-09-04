package com.tosware.nkm.services.http.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.StandardRoute
import com.tosware.nkm.actors.User
import com.tosware.nkm.actors.User.{RegisterFailure, RegisterSuccess}
import com.tosware.nkm.models.{AuthResponse, Credentials, RegisterRequest}
import com.tosware.nkm.services.UserService
import com.tosware.nkm.services.http.directives.{JwtDirective, JwtSecretKey}
import com.tosware.nkm.{Logging, NkmDependencies}
import spray.json.*

class AuthRoutes(deps: NkmDependencies)
  extends JwtDirective
    with SprayJsonSupport
    with Logging
{
  val jwtSecretKey: JwtSecretKey = deps.jwtSecretKey
  val userService: UserService = deps.userService

  def handleLoginEvent(event: User.LoginEvent): StandardRoute = event match {
    case User.LoginSuccess(userStateView) => complete(StatusCodes.OK, AuthResponse(getToken(userStateView.toJson.toString), userStateView))
    case User.LoginFailure(reason) => complete(StatusCodes.Unauthorized, reason)
  }

  val authPostRoutes = concat(
    path("register") {
      entity(as[RegisterRequest]) { entity =>
        logger.info(s"Received register request for ${entity.email}")
        userService.register(entity) match {
          case RegisterSuccess => complete(StatusCodes.Created)
          case RegisterFailure => complete(StatusCodes.Conflict) // TODO - change status code based on failure
          case _ => complete(StatusCodes.InternalServerError)
        }
      }
    },
    path("login") {
      entity(as[Credentials]) { entity =>
        logger.info(s"Logging in ${entity.email}")
        handleLoginEvent(userService.authenticate(entity))
      }
    },
    path("oauth-google") {
      entity(as[String]) { entity =>
        logger.info(s"Google oauth request")
        handleLoginEvent(userService.authenticateOauthGoogle(entity))
      }
    }

  )
}
