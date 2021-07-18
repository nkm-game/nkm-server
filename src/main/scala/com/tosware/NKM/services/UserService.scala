package com.tosware.NKM.services

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import com.tosware.NKM.{DBManager, NKMTimeouts}
import com.tosware.NKM.actors.User
import com.tosware.NKM.actors.User._
import com.tosware.NKM.models.{Credentials, RegisterRequest}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Await

object UserService extends NKMTimeouts {
  sealed trait Event
  case class LoggedIn(login: String) extends Event
  case object InvalidCredentials extends Event

  val db = Database.forConfig("slick.db")

  def authenticate(creds: Credentials)(implicit system: ActorSystem): Event = {
    val userActor: ActorRef = system.actorOf(User.props(creds.login))
    Await.result(userActor ? CheckLogin(creds.password), atMost) match {
      case LoginSuccess => LoggedIn(creds.login)
      case LoginFailure => InvalidCredentials
    }
  }

  def register(request: RegisterRequest)(implicit system: ActorSystem): RegisterEvent = {
    val userActor: ActorRef = system.actorOf(User.props(request.login))
    val emailExistsAction = DBManager.users.filter(_.email === Option(request.email)).exists.result
    val emailExists = Await.result(db.run(emailExistsAction), atMost)
    println(emailExistsAction.statements)
    println(emailExists)

    if(emailExists) RegisterFailure
    else Await.result(userActor ? Register(request.email, request.password), atMost).asInstanceOf[RegisterEvent]
  }
}
