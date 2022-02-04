package com.tosware.NKM.services.http.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import com.tosware.NKM.services.NKMDataService
import com.tosware.NKM.services.http.directives.JwtDirective

trait NKMDataRoutes extends JwtDirective
  with SprayJsonSupport
{
  implicit val NKMDataService: NKMDataService

  val nkmDataGetRoutes = concat(
    path("maps") {
      complete(NKMDataService.getHexMaps)
    },
    path("characters") {
      complete(NKMDataService.getCharactersMetadata)
    },
  )
}
