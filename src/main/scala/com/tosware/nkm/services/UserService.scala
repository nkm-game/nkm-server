package com.tosware.nkm.services

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.persistence.jdbc.query.scaladsl.JdbcReadJournal
import akka.persistence.query.PersistenceQuery
import akka.stream.scaladsl.Sink
import com.tosware.nkm.NkmTimeouts
import com.tosware.nkm.actors.User
import com.tosware.nkm.actors.User._
import com.tosware.nkm.models.{Credentials, RegisterRequest}
import slick.jdbc.JdbcBackend

object UserService {
  sealed trait Event
  case class LoggedIn(login: String) extends Event
  case object InvalidCredentials extends Event
}

class UserService(implicit db: JdbcBackend.Database, system: ActorSystem) extends NkmTimeouts {
  import UserService._

  def authenticate(creds: Credentials): Event = {
    val userActor: ActorRef = system.actorOf(User.props(creds.login))
    aw(userActor ? CheckLogin(creds.password)) match {
      case LoginSuccess => LoggedIn(creds.login)
      case LoginFailure => InvalidCredentials
    }
  }

  def register(request: RegisterRequest): RegisterEvent = {
    val readJournal: JdbcReadJournal = PersistenceQuery(system).readJournalFor[JdbcReadJournal](JdbcReadJournal.Identifier)
    val userActor: ActorRef = system.actorOf(User.props(request.login))
    val allEmailsFuture = readJournal.
      currentEventsByTag("register", 0)
      .map(_.event.asInstanceOf[RegisterSuccess].email)
      .runWith(Sink.seq[String])

    val allEmails = aw(allEmailsFuture)
    val emailExists = allEmails.contains(request.email)

    if(emailExists) RegisterFailure
    else aw(userActor ? Register(request.email, request.password)).asInstanceOf[RegisterEvent]
  }
}
