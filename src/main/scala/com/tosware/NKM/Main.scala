package com.tosware.NKM

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.tosware.NKM.actors.CQRSEventHandler
import com.tosware.NKM.services._
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

  DBManager.createNeededTables(db)


  // subscribe to events
  system.actorOf(CQRSEventHandler.props(db))

  val port = sys.env.getOrElse("PORT", "8080").toInt

  Http().newServerAt("0.0.0.0", port).bind(routes)
  println("Started http server")
}
