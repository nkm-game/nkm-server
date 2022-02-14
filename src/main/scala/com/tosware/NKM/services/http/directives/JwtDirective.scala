package com.tosware.NKM.services.http.directives

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.{complete, optionalHeaderValueByName, provide}
import com.tosware.NKM.models.JwtContent
import com.tosware.NKM.serializers.NKMJsonProtocol
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim, JwtSprayJson}
import spray.json._

import java.time.Instant
import scala.util.{Failure, Success}

case class JwtSecretKey(value: String)

trait JwtDirective extends NKMJsonProtocol {
  implicit val jwtSecretKey: JwtSecretKey

  /** returns Some(username) when authenticated, otherwise None */
  def authenticateToken(bearerToken: String): Option[String] = {
    val token = bearerToken.split(' ')(1)
    JwtSprayJson.decode(token, jwtSecretKey.value, Seq(JwtAlgorithm.HS256)) match {
      case Success(jwtClaim) =>
        val username = jwtClaim.content.parseJson.convertTo[JwtContent].content
        Some(username)
      case Failure(_) => None
    }
  }


  def authenticated: Directive1[String] =
    optionalHeaderValueByName("Authorization").flatMap {
      case Some(bearerToken) =>
        authenticateToken(bearerToken) match {
          case Some(username) => provide(username)
          case None => complete(StatusCodes.Unauthorized)
        }
      case _ => complete(StatusCodes.Unauthorized)
    }

  def getToken(login: String): String = {
    val claim: JwtClaim = JwtClaim(
      content = JwtContent(login).toJson.toString,
      expiration = Some(Instant.now.plusSeconds(157784760).getEpochSecond),
      issuedAt = Some(Instant.now.getEpochSecond)
    )
    val token = Jwt.encode(claim, jwtSecretKey.value, JwtAlgorithm.HS256)
    token
  }
}
