package com.tosware.NKM.actors

import akka.actor.{Actor, ActorLogging, Props}
import com.tosware.NKM.DBManager
import com.tosware.NKM.models.{LobbyState, UserState}
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
    context.system.eventStream.subscribe(self, classOf[Lobby.Event])
  }
  override def receive = {
    case User.RegisterSuccess(login, email, passwordHash) =>
      val insertAction = DBManager.users += UserState(login, Some(email), Some(passwordHash))
      Await.result(db.run(insertAction), DBManager.dbTimeout)
    case Lobby.CreateSuccess(id, name, hostUserId, creationDate) =>
      val insertAction = DBManager.lobbies += LobbyState(id, Some(name), Some(hostUserId), Some(creationDate), List(hostUserId))
      Await.result(db.run(insertAction), DBManager.dbTimeout)
  }
}
