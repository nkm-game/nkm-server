package com.tosware.nkm.actors

import akka.actor.{ActorLogging, Props}
import akka.persistence.journal.Tagged
import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.github.t3hnar.bcrypt.*
import com.softwaremill.quicklens.ModifyPimp
import com.tosware.nkm.NkmTimeouts
import com.tosware.nkm.models.*
import com.tosware.nkm.models.user.{UserState, UserStateView}

object User extends NkmTimeouts {
  sealed trait Query
  case object GetState extends Query

  sealed trait Command
  case class Register(password: String) extends Command
  case class RegisterHash(passwordHash: String) extends Command
  case class CheckLogin(password: String) extends Command
  case class OauthLogin() extends Command
  case object GrantAdmin extends Command
  case class SetPreferredColor(colorOpt: Option[NkmColor]) extends Command
  case class SetLanguage(language: String) extends Command

  sealed trait Event
  sealed trait RegisterEvent extends Event
  sealed trait LoginEvent extends Event

  case class RegisterSuccess(email: String, passwordHash: String) extends RegisterEvent
  case class OauthRegisterSuccess(email: String) extends RegisterEvent
  case object RegisterSuccess extends RegisterEvent
  case object RegisterFailure extends RegisterEvent

  case class LoginSuccess(userStateView: UserStateView) extends LoginEvent
  case class LoginFailure(reason: String) extends LoginEvent

  case class AdminGranted(email: String) extends Event
  case class PreferredColorSet(colorOpt: Option[NkmColor]) extends Event
  case class LanguageSet(language: String) extends Event

  def props(email: String): Props = Props(new User(email))

  def isEmailValid(email: String): Boolean = {
    val emailRegex =
      """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r
    email match {
      case null                                          => false
      case e if e.trim.isEmpty                           => false
      case e if emailRegex.findFirstMatchIn(e).isDefined => true
      case _                                             => false
    }
  }

  val registerTag = "register"
  val oauthRegisterTag = "oauth-register"
}

class User(email: String) extends PersistentActor with ActorLogging {
  import User.*
  override def persistenceId: String = s"user-$email"

  var userState: UserState = UserState(email)

  def register(email: String, passwordHash: String): Unit =
    userState = userState.copy(passwordHashOpt = Some(passwordHash), userId = Some(email), isRegistered = true)

  def oauthRegister(email: String): Unit =
    userState = userState.copy(userId = Some(email), isRegistered = true)

  def grantAdmin(): Unit =
    userState = userState.copy(isAdmin = true)

  def registerHash(passwordHash: String) =
    if (userState.isRegistered || !isEmailValid(email)) {
      sender() ! RegisterFailure
    } else {
      val e = RegisterSuccess(email, passwordHash)
      val taggedE = Tagged(e, Set(registerTag))
      persist(taggedE) { _ =>
        context.system.eventStream.publish(e)
        register(email, passwordHash)
        log.info(s"Persisted user: $email")
        sender() ! RegisterSuccess
      }
    }

  def checkLogin(password: String): LoginEvent = {
    if (!userState.isRegistered)
      return LoginFailure("User does not exist.")

    if (!userState.passwordHashOpt.exists(password.isBcryptedBounded))
      return LoginFailure("Invalid credentials.")

    LoginSuccess(userState.toView)
  }

  def setPreferredColor(colorOpt: Option[NkmColor]): Unit =
    userState = userState
      .modify(_.userSettings.preferredColor)
      .setTo(colorOpt)

  def setLanguage(language: String): Unit =
    userState = userState
      .modify(_.userSettings.language)
      .setTo(language)

  override def receive: Receive = {
    case GetState =>
      log.info("Received state request")
      sender() ! userState
    case Register(password) =>
      log.info(s"Register request for: $email")
      registerHash(password.boundedBcrypt)
    case RegisterHash(passwordHash) =>
      log.debug(s"Register via hash request for: $email")
      registerHash(passwordHash)
    case CheckLogin(password) =>
      log.info(s"Login check request for: $email")
      sender() ! checkLogin(password)
    case OauthLogin() =>
      log.info(s"Oauth login request for: $email")
      if (!userState.isRegistered) {
        val e = OauthRegisterSuccess(email)
        val taggedE = Tagged(e, Set(oauthRegisterTag))
        persist(taggedE) { _ =>
          oauthRegister(email)
          log.info(s"Persisted user: $email")
        }
      }
      sender() ! LoginSuccess(userState.toView)
    case GrantAdmin =>
      log.info(s"Granting admin to: $email")
      if (userState.isAdmin) {
        sender() ! CommandResponse.Failure("User is already admin.")
      } else {
        val e = AdminGranted(email)
        persist(e) { _ =>
          grantAdmin()
          log.info(s"Granted admin for: $email")
        }
        sender() ! CommandResponse.Success()
      }
    case SetPreferredColor(colorOpt) =>
      persist(PreferredColorSet(colorOpt)) { _ =>
        setPreferredColor(colorOpt)
      }
      sender() ! CommandResponse.Success()
    case SetLanguage(language) =>
      persist(LanguageSet(language)) { _ =>
        setLanguage(language)
      }
      sender() ! CommandResponse.Success()
    case e => log.warning(s"Unknown message: $e")
  }

  override def receiveRecover: Receive = {
    case RegisterSuccess(_, passwordHash) =>
      register(email, passwordHash)
      log.debug(s"Recovered register of $email")
    case OauthRegisterSuccess(_) =>
      oauthRegister(email)
      log.debug(s"Recovered oauth register of $email")
    case AdminGranted(_) =>
      grantAdmin()
      log.debug(s"Recovered grant admin")
    case PreferredColorSet(colorOpt) =>
      setPreferredColor(colorOpt)
    case LanguageSet(language) =>
      setLanguage(language)
    case RecoveryCompleted =>
    case e                 => log.warning(s"Unknown message: $e")
  }

  override def receiveCommand: Receive = {
    case _ =>
  }
}
