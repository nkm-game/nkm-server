package com.tosware.nkm.services.http.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.{Route, StandardRoute}
import com.tosware.nkm.actors.User
import com.tosware.nkm.actors.User.{RegisterFailure, RegisterSuccess}
import com.tosware.nkm.models.CommandResponse
import com.tosware.nkm.models.user.response.AuthResponse
import com.tosware.nkm.services.UserService
import com.tosware.nkm.services.http.directives.{JwtDirective, JwtSecretKey}
import com.tosware.nkm.{Logging, NkmDependencies}
import spray.json.*

object UserRequest {
  final case class SetLanguage(language: String)
  final case class SetPreferredColor(colorOpt: Option[String])
  final case class Login(email: String, password: String)
  final case class Register(email: String, password: String)
}

class UserRoutes(deps: NkmDependencies)
    extends JwtDirective
    with SprayJsonSupport
    with Logging {
  import UserRequest._

  val jwtSecretKey: JwtSecretKey = deps.jwtSecretKey
  val userService: UserService = deps.userService

  def handleLoginEvent(event: User.LoginEvent): StandardRoute = event match {
    case User.LoginSuccess(userStateView) =>
      complete(StatusCodes.OK, AuthResponse(getToken(userStateView.toJson.toString), userStateView))
    case User.LoginFailure(reason) => complete(StatusCodes.Unauthorized, reason)
  }

  val userRoutes: Route = pathPrefix("user") {
    concat(
      pathPrefix("settings") {
        authenticated { user =>
          path("fetch") {
            get {
              complete(userService.getUserSettings(user.email))
            }
          } ~
            path("set_preferred_color") {
              post {
                entity(as[UserRequest.SetPreferredColor]) { request =>
                  if (userService.isColorAvailable(request.colorOpt)) {
                    onSuccess(userService.setPreferredColor(user.email, request.colorOpt)) {
                      case CommandResponse.Success(_)   => complete(StatusCodes.NoContent)
                      case CommandResponse.Failure(msg) => complete(StatusCodes.InternalServerError -> msg)
                    }
                  } else complete(StatusCodes.BadRequest -> "Color not available")
                }
              }
            } ~
            path("set_language") {
              post {
                entity(as[UserRequest.SetLanguage]) { request =>
                  if (userService.isLanguageAvailable(request.language)) {
                    onSuccess(userService.setLanguage(user.email, request.language)) {
                      case CommandResponse.Success(_)   => complete(StatusCodes.NoContent)
                      case CommandResponse.Failure(msg) => complete(StatusCodes.InternalServerError -> msg)
                    }
                  } else complete(StatusCodes.BadRequest -> "Language not available")
                }
              }
            }
        }
      }
    )
  }

  val authPostRoutes: Route = concat(
    path("register") {
      entity(as[Register]) { entity =>
        logger.info(s"Received register request for ${entity.email}")
        userService.register(entity) match {
          case RegisterSuccess => complete(StatusCodes.Created)
          case RegisterFailure => complete(StatusCodes.Conflict) // TODO - change status code based on failure
          case _               => complete(StatusCodes.InternalServerError)
        }
      }
    },
    path("login") {
      entity(as[Login]) { entity =>
        logger.info(s"Logging in ${entity.email}")
        handleLoginEvent(userService.authenticate(entity))
      }
    },
    path("oauth-google") {
      entity(as[String]) { entity =>
        logger.info(s"Google oauth request")
        handleLoginEvent(userService.authenticateOauthGoogle(entity))
      }
    },
  )
}
