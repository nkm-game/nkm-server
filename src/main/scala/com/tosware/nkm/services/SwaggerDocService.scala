package com.tosware.nkm.services

import com.github.swagger.akka.*
import com.github.swagger.akka.model.Info
import com.tosware.nkm.services.http.routes.{BugReportRoutes, GameRoutes, LobbyRoutes, NkmDataRoutes, UserRoutes}

class SwaggerDocService(port: Int) extends SwaggerHttpService {
  override val apiClasses: Set[Class[?]] = Set(
    classOf[UserRoutes],
    classOf[LobbyRoutes],
    classOf[GameRoutes],
    classOf[NkmDataRoutes],
    classOf[BugReportRoutes],
  )
  override val host: String = s"localhost:$port"
  override val info: Info = Info(version = "1.0", title = "NKM API")
}
