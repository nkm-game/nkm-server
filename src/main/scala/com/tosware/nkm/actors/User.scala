package com.tosware.nkm.actors

import akka.actor.{ActorLogging, Props}
import akka.persistence.journal.Tagged
import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.github.t3hnar.bcrypt._
import com.tosware.nkm.NkmTimeouts
import com.tosware.nkm.models._

object User extends NkmTimeouts {
  sealed trait Query
  case object GetState extends Query

  sealed trait Command
  case class Register(email: String, password: String) extends Command
  case class CheckLogin(password: String) extends Command

  sealed trait Event
  sealed trait RegisterEvent extends Event
  sealed trait LoginEvent extends Event

  case class RegisterSuccess(login: String, email: String, passwordHash: String) extends RegisterEvent
  case object RegisterSuccess extends RegisterEvent
  case object RegisterFailure extends RegisterEvent

  case object LoginSuccess extends LoginEvent
  case object LoginFailure extends LoginEvent

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
        val passwordHash = password.boundedBcrypt
        val e = RegisterSuccess(login, email, passwordHash)
        val taggedE = Tagged(e, Set("register"))
        persist(taggedE) { _ =>
          context.system.eventStream.publish(e)
          register(email, passwordHash)
          log.info(s"Persisted user: $login")
          sender() ! RegisterSuccess
        }
      }
    case CheckLogin(password) =>
      log.info(s"Login check request for: $login")
      sender () ! {
        if(userState.registered() && password.isBcryptedBounded(userState.passwordHash.get)) LoginSuccess
        else LoginFailure
      }
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