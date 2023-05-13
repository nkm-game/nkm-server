package com.tosware.nkm.services.http.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.*
import com.tosware.nkm.actors.User.{RegisterFailure, RegisterSuccess}
import com.tosware.nkm.models.{Credentials, RegisterRequest}
import com.tosware.nkm.services.UserService
import com.tosware.nkm.services.UserService.*
import com.tosware.nkm.services.http.directives.{JwtDirective, JwtSecretKey}
import com.tosware.nkm.{Logging, NkmDependencies}

class AuthRoutes(deps: NkmDependencies)
  extends JwtDirective
    with SprayJsonSupport
    with Logging
{
  val jwtSecretKey: JwtSecretKey = deps.jwtSecretKey
  val userService: UserService = deps.userService

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
        userService.authenticate(entity) match {
          case LoggedIn(userId) => complete(StatusCodes.OK, getToken(userId))
          case InvalidCredentials => complete(StatusCodes.Unauthorized, "invalid credentials")
          case InternalError => complete(StatusCodes.InternalServerError)
        }
      }
    },
    path("oauth-google") {
      entity(as[String]) { entity =>
        logger.info(s"Google oauth request")
        userService.authenticateOauthGoogle(entity) match {
          case LoggedIn(userId) => complete(StatusCodes.OK, getToken(userId))
          case InvalidCredentials => complete(StatusCodes.Unauthorized, "invalid credentials")
          case InternalError => complete(StatusCodes.InternalServerError)
        }
      }
    }

  )
}
