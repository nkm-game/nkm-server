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

object Main extends App {
  val db: JdbcBackend.Database = Database.forConfig("slick.db")
  val port = sys.env.getOrElse("PORT", "8080").toInt

  try {
    val config: Config = ConfigFactory.load()
    val dbName = config.getString("slick.db.dbName")
    DBManager.createDbIfNotExists(dbName)
    DBManager.createNeededTables(db)

    implicit val system: ActorSystem = ActorSystem("NKMServer")

    val deps = new NKMDependencies(system, db)
    val httpService = new HttpService(deps)

    Http().newServerAt("0.0.0.0", port).bind(httpService.routes)
    println("Started http server")
  } catch {
    case e: Throwable =>
      println(e.printStackTrace())
      System.exit(1)
  }
}
