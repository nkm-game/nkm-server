package com.tosware.nkm

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.tosware.nkm.services.http.HttpService
import com.typesafe.config.{Config, ConfigFactory}
import slick.jdbc.JdbcBackend
import slick.jdbc.JdbcBackend.Database

import scala.annotation.tailrec

object Main extends App with Logging {
  val db: JdbcBackend.Database = Database.forConfig("slick.db")
  val port = sys.env.getOrElse("PORT", "8080").toInt

  @tailrec
  def initDb(lastDelay: Int = 0): Unit = {
    try {
      val config: Config = ConfigFactory.load()
      val dbName = config.getString("slick.db.dbName")
      DBManager.createDbIfNotExists(dbName)
      DBManager.createNeededTables(db)
    } catch {
      case e: Throwable =>
        if(lastDelay > 60) {
          e.printStackTrace()
          System.exit(1)
        }
        logger.error(s"Initializing database failed, retrying in ${lastDelay + 1} seconds...")
        Thread.sleep((lastDelay + 1) * 1000)
        initDb(lastDelay + 1)
    }
  }

  initDb()

  implicit val system: ActorSystem = ActorSystem("nkm-actor-system")
  val deps = new NkmDependencies(system, db)
  val httpService = new HttpService(deps)

  try {
    Http().newServerAt("0.0.0.0", port).bind(httpService.routes)
    logger.info("Started http server")
  } catch {
    case e: Throwable =>
      e.printStackTrace()
      System.exit(1)
  }
}
