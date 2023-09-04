package com.tosware.nkm.services.http.directives

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{authorize, complete, optionalHeaderValueByName, provide}
import akka.http.scaladsl.server.{Directive0, Directive1}
import com.tosware.nkm.models.{JwtContent, UserStateView}
import com.tosware.nkm.serializers.NkmJsonProtocol
import pdi.jwt.*
import spray.json.*

import java.time.Instant
import scala.util.{Failure, Success}

case class JwtSecretKey(value: String)

trait JwtHelper extends NkmJsonProtocol {
  implicit val jwtSecretKey: JwtSecretKey

  /** returns Some(userStateView) when authenticated, otherwise None */
  def authenticateToken(token: String): Option[UserStateView] = {
    JwtSprayJson.decode(token, jwtSecretKey.value, Seq(JwtAlgorithm.HS256)) match {
      case Success(jwtClaim) =>
        val userStateView = jwtClaim.content
          .parseJson.convertTo[JwtContent].content
          .parseJson.convertTo[UserStateView]
        Some(userStateView)
      case Failure(_) => None
    }
  }

  /** returns Some(userStateView) when authenticated, otherwise None */
  def authenticateBearerToken(bearerToken: String): Option[UserStateView] = {
    val token = bearerToken.split(' ')(1)
    authenticateToken(token)
  }

  /** returns token for given user state view */
  def getToken(userStateView: String): String = {
    val claim: JwtClaim = JwtClaim(
      content = JwtContent(userStateView).toJson.toString,
      expiration = Some(Instant.now.plusSeconds(157784760).getEpochSecond),
      issuedAt = Some(Instant.now.getEpochSecond)
    )
    val token = Jwt.encode(claim, jwtSecretKey.value, JwtAlgorithm.HS256)
    token
  }

}

trait JwtDirective extends JwtHelper {
  implicit val jwtSecretKey: JwtSecretKey

  def authenticated: Directive1[UserStateView] =
    optionalHeaderValueByName("Authorization").flatMap {
      case Some(bearerToken) =>
        authenticateBearerToken(bearerToken) match {
          case Some(userStateView) => provide(userStateView)
          case None => complete(StatusCodes.Unauthorized)
        }
      case _ => complete(StatusCodes.Unauthorized)
    }

  def authenticatedOpt: Directive1[Option[UserStateView]] =
    optionalHeaderValueByName("Authorization").flatMap {
      case Some(bearerToken) =>
        authenticateBearerToken(bearerToken) match {
          case Some(userStateView) => provide(Some(userStateView))
          case None => complete(StatusCodes.Unauthorized)
        }
      case _ => provide(None)
    }

  def requireAdmin: Directive0 =
    authenticated.flatMap { userStateView =>
      authorize(userStateView.isAdmin)
    }
}
