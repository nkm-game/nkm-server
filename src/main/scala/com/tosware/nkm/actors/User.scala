package com.tosware.nkm.actors

import akka.actor.{ActorLogging, Props}
import akka.persistence.journal.Tagged
import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.github.t3hnar.bcrypt.*
import com.tosware.nkm.NkmTimeouts
import com.tosware.nkm.models.*

object User extends NkmTimeouts {
  sealed trait Query
  case object GetState extends Query

  sealed trait Command
  case class Register(password: String) extends Command
  case class CheckLogin(password: String) extends Command
  case class OauthLogin() extends Command

  sealed trait Event
  sealed trait RegisterEvent extends Event
  sealed trait LoginEvent extends Event

  case class RegisterSuccess(email: String, passwordHash: String) extends RegisterEvent
  case class OauthRegisterSuccess(email: String) extends RegisterEvent
  case object RegisterSuccess extends RegisterEvent
  case object RegisterFailure extends RegisterEvent

  case object LoginSuccess extends LoginEvent
  case object LoginFailure extends LoginEvent

  def props(email: String): Props = Props(new User(email))

  def isEmailValid(email: String): Boolean = {
    val emailRegex = """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r
    email match {
      case null => false
      case e if e.trim.isEmpty => false
      case e if emailRegex.findFirstMatchIn(e).isDefined => true
      case _ => false
    }
  }

  val registerTag = "register"
  val oauthRegisterTag = "oauth-register"
}

class User(email: String) extends PersistentActor with ActorLogging {
  import User.*
  override def persistenceId: String = s"user-$email"

  var userState: UserState = UserState(email)

  def register(email: String, passwordHash: String): Unit = {
    userState = userState.copy(passwordHash = Some(passwordHash), userId = Some(email), registered = true)
  }

  def oauthRegister(email: String): Unit = {
    userState = userState.copy(userId = Some(email), registered = true)
  }

  override def receive: Receive = {
    case GetState =>
      log.info("Received state request")
      sender() ! userState
    case Register(password) =>
      log.info(s"Register request for: $email")
      if (userState.registered || !isEmailValid(email)) {
        sender() ! RegisterFailure
      } else {
        val passwordHash = password.boundedBcrypt
        val e = RegisterSuccess(email, passwordHash)
        val taggedE = Tagged(e, Set(registerTag))
        persist(taggedE) { _ =>
          context.system.eventStream.publish(e)
          register(email, passwordHash)
          log.info(s"Persisted user: $email")
          sender() ! RegisterSuccess
        }
      }
    case CheckLogin(password) =>
      log.info(s"Login check request for: $email")
      sender () ! {
        if(userState.registered && userState.passwordHash.isDefined && password.isBcryptedBounded(userState.passwordHash.get)) LoginSuccess
        else LoginFailure
      }
    case OauthLogin() =>
      log.info(s"Oauth login request for: $email")
      if(!userState.registered) {
        val e = OauthRegisterSuccess(email)
        val taggedE = Tagged(e, Set(oauthRegisterTag))
        persist(taggedE) { _ =>
          context.system.eventStream.publish(e)
          oauthRegister(email)
          log.info(s"Persisted user: $email")
        }
      }
      sender () ! LoginSuccess
    case e => log.warning(s"Unknown message: $e")
  }

  override def receiveRecover: Receive = {
    case RegisterSuccess(email, passwordHash) =>
      register(email, passwordHash)
      log.debug(s"Recovered register of $email")
    case OauthRegisterSuccess(email) =>
      oauthRegister(email)
      log.debug(s"Recovered oauth register of $email")
    case RecoveryCompleted =>
    case e => log.warning(s"Unknown message: $e")
  }

  override def receiveCommand: Receive = {
    case _ =>
  }
}