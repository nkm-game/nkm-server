package com.tosware.NKM.services

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.persistence.jdbc.query.scaladsl.JdbcReadJournal
import akka.persistence.query.PersistenceQuery
import akka.stream.scaladsl.Sink
import com.tosware.NKM.actors.User
import com.tosware.NKM.actors.User._
import com.tosware.NKM.models.{Credentials, RegisterRequest}
import com.tosware.NKM.{DBManager, NKMTimeouts}
import slick.jdbc.JdbcBackend
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Await

object UserService {
  sealed trait Event
  case class LoggedIn(login: String) extends Event
  case object InvalidCredentials extends Event
}

class UserService(implicit db: JdbcBackend.Database) extends NKMTimeouts {
  import UserService._

  def authenticate(creds: Credentials)(implicit system: ActorSystem): Event = {
    val userActor: ActorRef = system.actorOf(User.props(creds.login))
    Await.result(userActor ? CheckLogin(creds.password), atMost) match {
      case LoginSuccess => LoggedIn(creds.login)
      case LoginFailure => InvalidCredentials
    }
  }

  def register(request: RegisterRequest)(implicit system: ActorSystem): RegisterEvent = {
    val readJournal: JdbcReadJournal = PersistenceQuery(system).readJournalFor[JdbcReadJournal](JdbcReadJournal.Identifier)
    val userActor: ActorRef = system.actorOf(User.props(request.login))
    val allEmailsFuture = readJournal.
      currentEventsByTag("register", 0)
      .map(_.event.asInstanceOf[RegisterSuccess].email)
      .runWith(Sink.seq[String])

    val allEmails = Await.result(allEmailsFuture, atMost)
    val emailExists = allEmails.contains(request.email)

    if(emailExists) RegisterFailure
    else Await.result(userActor ? Register(request.email, request.password), atMost).asInstanceOf[RegisterEvent]
  }
}
