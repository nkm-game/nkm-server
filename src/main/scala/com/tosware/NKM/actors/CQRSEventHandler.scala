package com.tosware.NKM.actors

import akka.actor.{Actor, ActorLogging, Props}
import com.tosware.NKM.DBManager
import com.tosware.NKM.models.UserState
import com.tosware.NKM.models.lobby.LobbyState
import slick.jdbc.JdbcBackend
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.MySQLProfile.api._
import spray.json._

import scala.concurrent.Await

object CQRSEventHandler {
  def props(db: JdbcBackend.Database): Props = Props(new CQRSEventHandler(db))
}

class CQRSEventHandler(db: JdbcBackend.Database)
  extends Actor
    with ActorLogging
    with DefaultJsonProtocol
{
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
      val insertAction = DBManager.lobbies += LobbyState(id, Some(name), Some(hostUserId), Some(creationDate), chosenHexMapName = None, List(hostUserId))
      Await.result(db.run(insertAction), DBManager.dbTimeout)
    case Lobby.UserJoined(id, userId) =>
      val q = for {l <- DBManager.lobbies if l.id === id} yield l.userIds
      val currentUserIds: List[String] = Await.result(db.run(q.result), DBManager.dbTimeout).head.parseJson.convertTo[List[String]]
      val userIdsUpdated = currentUserIds :+ userId
      val userIdsUpdatedString = userIdsUpdated.toJson.toString
      val updateAction = q.update(userIdsUpdatedString)
      Await.result(db.run(updateAction), DBManager.dbTimeout)
    case Lobby.UserLeft(id, userId) =>
      val q = for {l <- DBManager.lobbies if l.id === id} yield l.userIds
      val currentUserIds: List[String] = Await.result(db.run(q.result), DBManager.dbTimeout).head.parseJson.convertTo[List[String]]
      val userIdsUpdated = currentUserIds.filterNot(_ == userId)
      val userIdsUpdatedString = userIdsUpdated.toJson.toString
      val updateAction = q.update(userIdsUpdatedString)
      Await.result(db.run(updateAction), DBManager.dbTimeout)
    case Lobby.MapNameSet(id, hexMapName) =>
      val q = for {l <- DBManager.lobbies if l.id === id} yield l.chosenHexMapName
      val updateAction = q.update(Some(hexMapName))
      Await.result(db.run(updateAction), DBManager.dbTimeout)
  }
}
