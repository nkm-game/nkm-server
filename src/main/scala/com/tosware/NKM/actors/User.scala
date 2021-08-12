package com.tosware.NKM.actors

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.pattern.ask
import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.github.t3hnar.bcrypt._
import com.tosware.NKM.NKMTimeouts
import com.tosware.NKM.models._

import scala.concurrent.Await

object User extends NKMTimeouts {
  sealed trait Query
  case object GetState extends Query

  sealed trait Command
  case class Register(email: String, password: String) extends Command
  case class CheckLogin(password: String) extends Command
//  case class CreateNewGame(gameOpts: GameOptions) extends Command
//  case class CreateLobby(name: String) extends Command

  sealed trait Event
  sealed trait RegisterEvent extends Event
  sealed trait LoginEvent extends Event

  case class RegisterSuccess(login: String, email: String, passwordHash: String) extends RegisterEvent
  case object RegisterSuccess extends RegisterEvent
  case object RegisterFailure extends RegisterEvent

  case object LoginSuccess extends LoginEvent
  case object LoginFailure extends LoginEvent

//  case class LobbyCreated(lobbyId: String) extends Event
//  case object LobbyCreationFailure extends Event

  def props(login: String): Props = Props(new User(login))
}

class User(login: String) extends PersistentActor with ActorLogging {
  import User._
  override def persistenceId: String = s"user-$login"

  var userState: UserState = UserState(login)

  def register(email: String, passwordHash: String): Unit = {
    userState = userState.copy(passwordHash = Some(passwordHash), email = Some(email))
  }

  override def receive: Receive = {
    case GetState =>
      log.info("Received state request")
      sender() ! userState
    case Register(email, password) =>
      log.info(s"Register request for: $login")
      if (userState.registered()) {
        sender() ! RegisterFailure
      } else {
        val passwordHash = password.bcrypt
        persist(RegisterSuccess(login, email, passwordHash)) { _ =>
          register(email, passwordHash)
          context.system.eventStream.publish(RegisterSuccess(login, email, passwordHash))
          log.info(s"Persisted user: $login")
          sender() ! RegisterSuccess
        }
      }
    case CheckLogin(password) =>
      log.info(s"Login check request for: $login")
      sender () ! {
        if(userState.registered() && password.isBcrypted(userState.passwordHash.get)) LoginSuccess
        else LoginFailure
      }

//    case CreateLobby(name) =>
//      log.info(s"Received create lobby request")
//      val randomId = java.util.UUID.randomUUID.toString
//      val lobby: ActorRef = context.system.actorOf(Lobby.props(randomId))
//      val creationResult = Await.result(lobby ? Lobby.Create(name), atMost) match {
//        case Lobby.CreateSuccess => LobbyCreated(randomId)
//        case _ => LobbyCreationFailure
//      }
//      sender() ! creationResult
    case e => log.warning(s"Unknown message: $e")
  }

  override def receiveRecover: Receive = {
    case RegisterSuccess(login, email, passwordHash) =>
      register(email, passwordHash)
      log.debug(s"Recovered register")
    case RecoveryCompleted =>
    case e => log.warning(s"Unknown message: $e")
  }

  override def receiveCommand: Receive = {
    case _ =>
  }
}