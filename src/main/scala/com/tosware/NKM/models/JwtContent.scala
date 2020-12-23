package com.tosware.NKM.models

import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class JwtContent(content: String)

object JwtContent extends DefaultJsonProtocol {
  implicit val jwtClaimResponseFormat: RootJsonFormat[JwtContent] = jsonFormat1(JwtContent.apply)
}
