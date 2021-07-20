package com.tosware.NKM.actors

import akka.actor.{Actor, ActorLogging, Props}
import com.tosware.NKM.DBManager
import com.tosware.NKM.models.UserState
import slick.jdbc.JdbcBackend
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Await

object CQRSEventHandler {
  def props(db: JdbcBackend.Database): Props = Props(new CQRSEventHandler(db))
}

class CQRSEventHandler(db: JdbcBackend.Database) extends Actor with ActorLogging {
  override def preStart(): Unit = {
    log.info("CQRSEventHandler started")
    context.system.eventStream.subscribe(self, classOf[User.Event])
  }
  override def receive = {
    case registerSuccess @ User.RegisterSuccess(login, email, passwordHash) =>
      log.info("Register received in CQRS Event Handler")
      log.info(registerSuccess.toString)
      val insertAction = DBManager.users += UserState(login, Some(email), Some(passwordHash))
      Await.result(db.run(insertAction), DBManager.dbTimeout)
  }
}
