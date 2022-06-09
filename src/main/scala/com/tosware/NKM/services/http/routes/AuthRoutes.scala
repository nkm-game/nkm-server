package com.tosware.NKM.services.http.routes

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import com.tosware.NKM.NKMDependencies
import com.tosware.NKM.actors.User.{RegisterFailure, RegisterSuccess}
import com.tosware.NKM.models.{Credentials, RegisterRequest}
import com.tosware.NKM.services.UserService
import com.tosware.NKM.services.UserService.{InvalidCredentials, LoggedIn}
import com.tosware.NKM.services.http.directives.{JwtDirective, JwtSecretKey}

class AuthRoutes(deps: NKMDependencies) extends JwtDirective
    with SprayJsonSupport
{
  val jwtSecretKey: JwtSecretKey = deps.jwtSecretKey
  val userService: UserService = deps.userService

  val authPostRoutes = concat(
    path("register") {
      entity(as[RegisterRequest]) { entity =>
        println(s"Received register request for ${entity.login}")
        userService.register(entity) match {
          case RegisterSuccess => complete(StatusCodes.Created)
          case RegisterFailure => complete(StatusCodes.Conflict) // TODO - change status code based on failure
          case _ => complete(StatusCodes.InternalServerError)
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
    }
  )
}
