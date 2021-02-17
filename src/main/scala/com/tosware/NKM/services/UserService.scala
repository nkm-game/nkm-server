package com.tosware.NKM.services

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import com.tosware.NKM.actors.User
import com.tosware.NKM.actors.User.{CheckLogin, LoginFailure, LoginSuccess, Register, RegisterEvent, RegisterFailure, RegisterSuccess}
import com.tosware.NKM.models.{Credentials, RegisterRequest}

import scala.concurrent.Await
import scala.concurrent.duration._

object UserService {
  sealed trait Event
  case class LoggedIn(login: String) extends Event
  case object InvalidCredentials extends Event
  implicit val timeout: Timeout = Timeout(500 millis)

  def authenticate(creds: Credentials)(implicit system: ActorSystem): Event = {
    val userActor: ActorRef = system.actorOf(User.props(creds.login))
    Await.result(userActor ? CheckLogin(creds.password), 500 millis) match {
      case LoginSuccess => LoggedIn(creds.login)
      case LoginFailure => InvalidCredentials
    }
  }

  def register(request: RegisterRequest)(implicit system: ActorSystem): RegisterEvent = {
    val userActor: ActorRef = system.actorOf(User.props(request.login))
    // TODO: check if email exists
    Await.result(userActor ? Register(request.email, request.password), 500 millis).asInstanceOf[RegisterEvent]
  }
}
