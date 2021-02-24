package com.tosware.NKM.actors

import java.time.LocalDate

import akka.actor.{ActorLogging, Props}
import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.tosware.NKM.models._


object Lobby {
  sealed trait Query
  case object GetState extends Query

  sealed trait Command
  case class Create(name: String) extends Command
  case class UserJoin(userId: String) extends Command

  sealed trait Event

  case class CreateSuccess(name: String) extends Event
  case object CreateSuccess extends Event
  case object CreateFailure extends Event

  case class UserJoined(userId: String) extends Event

  def props(id: String): Props = Props(new Lobby(id))
}

class Lobby(id: String) extends PersistentActor with ActorLogging {
  import Lobby._
  override def persistenceId: String = s"lobby-$id"

  var lobbyState: LobbyState = LobbyState(id)

  def create(name: String): Unit = {
    lobbyState = lobbyState.copy(name = Some(name), creationDate = Some(LocalDate.now()))
  }

  override def receive: Receive = {
    case GetState =>
      log.info("Received state request")
      sender() ! lobbyState
    case Create(name) =>
      log.info(s"Received create request")
        if(!lobbyState.created()) {
          persist(CreateSuccess(name)) { _ =>
            log.info(s"Created lobby")
            create(name)
            sender() ! CreateSuccess
          }
        }
        else {
          sender() ! CreateFailure
        }
    case e => log.warning(s"Unknown message: $e")
  }
//
  override def receiveRecover: Receive = {
    case CreateSuccess(name) =>
      create(name)
      log.info(s"Recovered create")
    case RecoveryCompleted =>
    case e => log.warning(s"Unknown message: $e")
  }

  override def receiveCommand: Receive = {
    case _ =>
  }
}