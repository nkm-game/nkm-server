package com.tosware.NKM

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import com.tosware.NKM.actors.{CQRSEventHandler, User}
import com.tosware.NKM.services.HttpService
import slick.dbio.DBIO
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object Main extends App with HttpService {
  def setupDatabase(): Unit = {
    def createJournalIfNotExists = DBIO.seq(
      sqlu"""
    CREATE TABLE IF NOT EXISTS journal (
      ordering SERIAL,
      persistence_id VARCHAR(255) NOT NULL,
      sequence_number BIGINT NOT NULL,
      deleted BOOLEAN DEFAULT FALSE,
      tags VARCHAR(255) DEFAULT NULL,
      message BLOB NOT NULL,
      PRIMARY KEY(persistence_id, sequence_number),
      INDEX(ordering)
    )
    """,
    )
    def createSnapshotIfNotExists = DBIO.seq(
      sqlu"""
        CREATE TABLE IF NOT EXISTS snapshot (
          persistence_id VARCHAR(255) NOT NULL,
          sequence_number BIGINT NOT NULL,
          created BIGINT NOT NULL,
          snapshot BLOB NOT NULL,
          PRIMARY KEY (persistence_id, sequence_number)
        )
      """,
    )
    val db = Database.forConfig("slick.db")

    val setupAction: DBIO[Unit] = DBIO.seq(
      createJournalIfNotExists,
      createSnapshotIfNotExists,
    )
    val setupFuture = db.run(setupAction)
    Await.result(setupFuture, 5000.millis)
  }

  setupDatabase()
  override implicit val system: ActorSystem = ActorSystem("NKMServer")

  system.actorOf(CQRSEventHandler.props())

  sys.env.getOrElse("DEBUG", "false").toBooleanOption match {
    case Some(true) =>
      Http().newServerAt("0.0.0.0", 8080).bind(routes)
      println("Started http server")
    case _ =>
      Http().newServerAt("0.0.0.0", 8080).enableHttps(getHttps).bindFlow(routes)
      println("Started https server")
  }


}
