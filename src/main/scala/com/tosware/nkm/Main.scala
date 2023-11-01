package com.tosware.nkm

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import com.tosware.nkm.actors.User
import com.tosware.nkm.services.http.HttpService
import com.typesafe.config.{Config, ConfigFactory}
import slick.jdbc.JdbcBackend
import slick.jdbc.JdbcBackend.Database

import scala.annotation.tailrec

object Main extends App with Logging {
  val db: JdbcBackend.Database = Database.forConfig("slick.db")
  val port = sys.env.getOrElse("PORT", "3737").toInt

  implicit val system: ActorSystem = ActorSystem("nkm-actor-system")

  @tailrec
  def initDb(lastDelay: Int = 0): Unit =
    try {
      val config: Config = ConfigFactory.load()
      val dbName = config.getString("slick.db.dbName")
      DBManager.createDbIfNotExists(dbName)
      DBManager.createNeededTables(db)
    } catch {
      case e: Throwable =>
        if (lastDelay > 60) {
          e.printStackTrace()
          System.exit(1)
        }
        logger.error(s"Initializing database failed, retrying in ${lastDelay + 1} seconds...")
        Thread.sleep((lastDelay + 1) * 1000)
        initDb(lastDelay + 1)
    }

  // TODO: check if users are not already initialized
  def initUsers(): Unit = {
    val tojatosActor: ActorRef = system.actorOf(User.props("tojatos@gmail.com"))
    tojatosActor ! User.RegisterHash("$2a$10$PXY3u4hPEic7sbKMoVqhn.qoSRJFah36E1XiujvJnfZgyqxBm44zS")
    tojatosActor ! User.GrantAdmin

    val bigRedActor: ActorRef = system.actorOf(User.props("bigredchick01@gmail.com"))
    bigRedActor ! User.RegisterHash("$2a$10$Pi5AqJzxUez3Pmst5ilr2.8xw.QTftY121pofuitFpUUpwicAkF.y")
    bigRedActor ! User.GrantAdmin

    val testUserActor1: ActorRef = system.actorOf(User.props("test1@example.com"))
    val testUserActor2: ActorRef = system.actorOf(User.props("test2@example.com"))

    testUserActor1 ! User.Register("test")
    testUserActor2 ! User.Register("test")
  }

  initDb()
  initUsers()

  val deps = new NkmDependencies(system)
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
