package com.tosware.NKM.services.http.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import com.tosware.NKM.NKMDependencies
import com.tosware.NKM.services.NKMDataService
import com.tosware.NKM.services.http.directives.{JwtDirective, JwtSecretKey}

class NKMDataRoutes(deps: NKMDependencies) extends JwtDirective
  with SprayJsonSupport
{
  val jwtSecretKey: JwtSecretKey = deps.jwtSecretKey
  val NKMDataService: NKMDataService = deps.NKMDataService

  val nkmDataGetRoutes = concat(
    path("maps") {
      complete(NKMDataService.getHexMaps)
    },
    path("characters") {
      complete(NKMDataService.getCharacterMetadatas)
    },
    path("abilities") {
      complete(NKMDataService.getAbilityMetadatas)
    },
    path("character_effects") {
      complete(NKMDataService.getCharacterEffectMetadatas)
    },
  )
}
