package com.tosware.nkm.services.http.directives

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.{complete, optionalHeaderValueByName, provide}
import com.tosware.nkm.UserId
import com.tosware.nkm.models.JwtContent
import com.tosware.nkm.serializers.NkmJsonProtocol
import pdi.jwt.*
import spray.json.*

import java.time.Instant
import scala.util.{Failure, Success}

case class JwtSecretKey(value: String)

trait JwtHelper extends NkmJsonProtocol {
  implicit val jwtSecretKey: JwtSecretKey

  /** returns Some(userIdOpt) when authenticated, otherwise None */
  def authenticateToken(token: String): Option[UserId] = {
    JwtSprayJson.decode(token, jwtSecretKey.value, Seq(JwtAlgorithm.HS256)) match {
      case Success(jwtClaim) =>
        val userId = jwtClaim.content.parseJson.convertTo[JwtContent].content
        Some(userId)
      case Failure(_) => None
    }
  }

  /** returns Some(userIdOpt) when authenticated, otherwise None */
  def authenticateBearerToken(bearerToken: String): Option[UserId] = {
    val token = bearerToken.split(' ')(1)
    authenticateToken(token)
  }

  /** returns token for given userId */
  def getToken(userId: String): String = {
    val claim: JwtClaim = JwtClaim(
      content = JwtContent(userId).toJson.toString,
      expiration = Some(Instant.now.plusSeconds(157784760).getEpochSecond),
      issuedAt = Some(Instant.now.getEpochSecond)
    )
    val token = Jwt.encode(claim, jwtSecretKey.value, JwtAlgorithm.HS256)
    token
  }

}

trait JwtDirective extends JwtHelper {
  implicit val jwtSecretKey: JwtSecretKey

  def authenticated: Directive1[String] =
    optionalHeaderValueByName("Authorization").flatMap {
      case Some(bearerToken) =>
        authenticateBearerToken(bearerToken) match {
          case Some(username) => provide(username)
          case None => complete(StatusCodes.Unauthorized)
        }
      case _ => complete(StatusCodes.Unauthorized)
    }
}
