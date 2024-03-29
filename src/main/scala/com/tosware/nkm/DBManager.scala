package com.tosware.nkm

import com.typesafe.config.{Config, ConfigFactory}
import slick.dbio.DBIO
import slick.jdbc.JdbcBackend
import slick.jdbc.MySQLProfile.api.*

import scala.concurrent.Await
import scala.concurrent.duration.{DurationInt, FiniteDuration}

object DBManager {
  val dbTimeout: FiniteDuration = NkmConf.int("dbTimeout").millis

  def createDbIfNotExists(dbName: String): Unit = {
    val config: Config = ConfigFactory.load("slick_no_db.conf")
    val db = Database.forConfig("slick.db", config)
    val setupAction = DBIO.seq(sqlu"""CREATE DATABASE IF NOT EXISTS #$dbName;""")

    Await.result(db.run(setupAction), dbTimeout)
  }

  def dropAllTables(db: JdbcBackend.Database): Unit = {
    def dropActions = DBIO.seq(
      sqlu"""DROP TABLE IF EXISTS journal""",
      sqlu"""DROP TABLE IF EXISTS snapshot""",
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
    """
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
      """
    )

    val setupAction: DBIO[Unit] = DBIO.seq(
      createJournalIfNotExists,
      createSnapshotIfNotExists,
    )
    Await.result(db.run(setupAction), dbTimeout)
  }
}
