package com.tosware.NKM.services

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import com.tosware.NKM.NKMTimeouts
import com.tosware.NKM.actors.User
import com.tosware.NKM.actors.User._
import com.tosware.NKM.models.{Credentials, RegisterRequest}

import scala.concurrent.Await

object UserService extends NKMTimeouts {
  sealed trait Event
  case class LoggedIn(login: String) extends Event
  case object InvalidCredentials extends Event

  def authenticate(creds: Credentials)(implicit system: ActorSystem): Event = {
    val userActor: ActorRef = system.actorOf(User.props(creds.login))
    Await.result(userActor ? CheckLogin(creds.password), atMost) match {
      case LoginSuccess => LoggedIn(creds.login)
      case LoginFailure => InvalidCredentials
    }
  }

  def register(request: RegisterRequest)(implicit system: ActorSystem): RegisterEvent = {
    val userActor: ActorRef = system.actorOf(User.props(request.login))
    // TODO: check if email exists
    Await.result(userActor ? Register(request.email, request.password), atMost).asInstanceOf[RegisterEvent]
  }
}
