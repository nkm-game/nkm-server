package com.tosware.nkm.services.http.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives.*
import com.tosware.nkm.NkmDependencies
import com.tosware.nkm.services.NkmDataService
import com.tosware.nkm.services.http.directives.{JwtDirective, JwtSecretKey}

class NkmDataRoutes(deps: NkmDependencies) extends JwtDirective
    with SprayJsonSupport {
  val jwtSecretKey: JwtSecretKey = deps.jwtSecretKey
  val nkmDataService: NkmDataService = deps.nkmDataService

  val getRoutes = concat(
    path("colors") {
      complete(nkmDataService.getColorNames)
    },
    path("maps") {
      complete(nkmDataService.getHexMaps)
    },
    path("characters") {
      complete(nkmDataService.getCharacterMetadataSeq)
    },
    path("abilities") {
      complete(nkmDataService.getAbilityMetadataSeq.map(_.toMarshallable))
    },
    path("character_effects") {
      complete(nkmDataService.getCharacterEffectMetadataSeq)
    },
    path("hex_cell_effects") {
      complete(nkmDataService.getHexCellEffectMetadataSeq)
    },
    path("version") {
      complete(nkmDataService.getCurrentVersion)
    },
  )
}
