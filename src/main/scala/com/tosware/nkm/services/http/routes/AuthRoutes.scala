package com.tosware.nkm.services.http.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import com.tosware.nkm.actors.User.{RegisterFailure, RegisterSuccess}
import com.tosware.nkm.models.{Credentials, RegisterRequest}
import com.tosware.nkm.services.UserService
import com.tosware.nkm.services.UserService.{InvalidCredentials, LoggedIn}
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
        logger.info(s"Received register request for ${entity.login}")
        userService.register(entity) match {
          case RegisterSuccess => complete(StatusCodes.Created)
          case RegisterFailure => complete(StatusCodes.Conflict) // TODO - change status code based on failure
          case _ => complete(StatusCodes.InternalServerError)
        }
      }
    },
    path("login") {
      entity(as[Credentials]) { entity =>
        logger.info(s"Logging in ${entity.login}")
        userService.authenticate(entity) match {
          case LoggedIn(login) => complete(StatusCodes.OK, getToken(login))
          case InvalidCredentials => complete(StatusCodes.Unauthorized, "invalid credentials")
        }
      }
    }
  )
}
