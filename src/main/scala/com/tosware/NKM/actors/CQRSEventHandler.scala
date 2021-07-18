package com.tosware.NKM.actors

import akka.actor.{Actor, ActorLogging, Props}
import com.tosware.NKM.DBManager
import com.tosware.NKM.DBManager.dbTimeout
import com.tosware.NKM.models.UserState
import slick.dbio.DBIO
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Await

object CQRSEventHandler {
  def props(): Props = Props(new CQRSEventHandler())
}

class CQRSEventHandler extends Actor with ActorLogging {
  override def preStart(): Unit = {
    log.info("CQRSEventHandler started")
    context.system.eventStream.subscribe(self, classOf[User.Event])
  }
  override def receive = {
    case registerSuccess @ User.RegisterSuccess(login, email, passwordHash) =>
      val db = Database.forConfig("slick.db")
      log.info("Register received in CQRS Event Handler")
      log.info(registerSuccess.toString)
      val insertAction = DBManager.users += UserState(login, Some(email), Some(passwordHash))
      Await.result(db.run(insertAction), DBManager.dbTimeout)
  }
}
