package com.tosware.NKM

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import com.tosware.NKM.actors.{GamesManager, LobbiesManager}
import com.tosware.NKM.services._
import com.tosware.NKM.services.http.HttpService
import com.tosware.NKM.services.http.directives.JwtSecretKey
import com.typesafe.config.{Config, ConfigFactory}
import slick.jdbc.JdbcBackend
import slick.jdbc.JdbcBackend.Database

import scala.language.postfixOps

object Main extends App with HttpService {
  implicit val system: ActorSystem = ActorSystem("NKMServer")
  implicit val db: JdbcBackend.Database = Database.forConfig("slick.db")
  implicit val NKMDataService: NKMDataService = new NKMDataService()
  val gamesManagerActor: ActorRef = system.actorOf(GamesManager.props(NKMDataService))
  val lobbiesManagerActor: ActorRef = system.actorOf(LobbiesManager.props(NKMDataService))
  implicit val userService: UserService = new UserService()
  implicit val gameService: GameService = new GameService(gamesManagerActor)
  implicit val lobbyService: LobbyService = new LobbyService(lobbiesManagerActor)

  implicit val jwtSecretKey: JwtSecretKey = JwtSecretKey(sys.env.getOrElse("JWT_SECRET_KEY", "tmp_jwt_secret_key^*(^(*$#&(*"))
  val port = sys.env.getOrElse("PORT", "8080").toInt

  try {
    val config: Config = ConfigFactory.load()
    val dbName = config.getString("slick.db.dbName")
    DBManager.createDbIfNotExists(dbName)
    DBManager.createNeededTables(db)

    Http().newServerAt("0.0.0.0", port).bind(routes)
    println("Started http server")
  } catch {
    case e: Throwable =>
      println(e.printStackTrace())
      System.exit(1)
  }
}
