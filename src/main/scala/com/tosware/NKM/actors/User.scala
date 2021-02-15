package com.tosware.NKM.actors

import akka.actor.{ActorLogging, Props}
import akka.persistence.PersistentActor
import com.tosware.NKM.models._
import com.github.t3hnar.bcrypt._

object User {
  sealed trait Query
  case object GetState extends Query

  sealed trait Command
  case class Register(password: String) extends Command

  sealed trait Event
  sealed trait RegisterEvent extends Event
  case class RegisterSuccess(passwordHash: String) extends RegisterEvent
  case object RegisterSuccess extends RegisterEvent
  case object RegisterFailure extends RegisterEvent

  def props(login: String): Props = Props(new User(login))
}

class User(login: String) extends PersistentActor with ActorLogging {
  import User._
  override def persistenceId: String = s"user-$login"

  var userState: UserState = UserState(login)

  def register(passwordHash: String): Unit = {
    userState = userState.copy(passwordHash = Some(passwordHash))
  }
//  def register(password: String): RegisterEvent = {
//    if (userState.registered) {
//      RegisterFailure
//    } else {
//      userState = userState.copy(passwordHash = Some(password.bcrypt))
//      RegisterSuccess(password.bcrypt) //#TODO: Hash
//    }
//  }

  override def receive: Receive = {
    case GetState =>
      log.info("Received state request")
      sender() ! userState
    case Register(password) =>
      log.info(s"Register request for: $login")
      if (userState.registered()) {
        sender() ! RegisterFailure
      } else {
        val passwordHash = password.bcrypt
        persist(RegisterSuccess(passwordHash)) { _ =>
          register(passwordHash)
          log.info(s"Persisted user: $login")
          sender() ! RegisterSuccess
        }
      }
    case e => log.warning(s"Unknown message: $e")
  }

  override def receiveRecover: Receive = {
    case RegisterSuccess(passwordHash) =>
      register(passwordHash)
      log.info(s"Recovered register")
    case e => log.warning(s"Unknown message: $e")
  }

  override def receiveCommand: Receive = {
    case _ =>
  }
}