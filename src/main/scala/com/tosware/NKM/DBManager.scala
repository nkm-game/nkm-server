package com.tosware.NKM

import com.tosware.NKM.tables._
import slick.dbio.DBIO
import slick.jdbc.JdbcBackend
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object DBManager {
  val dbTimeout = 300.millis
  val users = TableQuery[User]
  val lobbies = TableQuery[Lobby]

  val queries = List(
    users,
    lobbies,
  )

  def dropAllTables(db: JdbcBackend.Database): Unit = {
    def dropActions = DBIO.seq(
      sqlu"""DROP TABLE IF EXISTS journal""",
      sqlu"""DROP TABLE IF EXISTS snapshot""",
      DBIO.sequence(queries.map(_.schema.dropIfExists)),
    )
    Await.result(db.run(dropActions), dbTimeout)
  }
  def createNeededTables(db: JdbcBackend.Database): Unit = {
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

    val setupAction: DBIO[Unit] = DBIO.seq(
      createJournalIfNotExists,
      createSnapshotIfNotExists,
      DBIO.sequence(queries.map(_.schema.createIfNotExists)),
    )
    Await.result(db.run(setupAction), dbTimeout)
  }
}
