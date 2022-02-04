package com.tosware.NKM

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.tosware.NKM.actors.CQRSEventHandler
import com.tosware.NKM.services._
import com.tosware.NKM.services.http.HttpService
import com.tosware.NKM.services.http.directives.JwtSecretKey
import slick.jdbc.JdbcBackend
import slick.jdbc.JdbcBackend.Database

import scala.language.postfixOps

object Main extends App with HttpService {
  implicit val system: ActorSystem = ActorSystem("NKMServer")
  implicit val db: JdbcBackend.Database = Database.forConfig("slick.db")
  implicit val NKMDataService: NKMDataService = new NKMDataService()
  implicit val userService: UserService = new UserService()
  implicit val lobbyService: LobbyService = new LobbyService()
  implicit val gameService: GameService = new GameService()

  implicit val jwtSecretKey: JwtSecretKey = JwtSecretKey(sys.env.getOrElse("JWT_SECRET_KEY", "tmp_jwt_secret_key^*(^(*$#&(*"))
  val port = sys.env.getOrElse("PORT", "8080").toInt

  try {
    DBManager.createNeededTables(db)

    // subscribe to events
    system.actorOf(CQRSEventHandler.props(db))

    Http().newServerAt("0.0.0.0", port).bind(routes)
    println("Started http server")
  } catch {
    case e: Throwable =>
      println(e.printStackTrace())
      System.exit(1)
  }
}
