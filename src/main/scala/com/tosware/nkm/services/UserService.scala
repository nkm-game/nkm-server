package com.tosware.nkm.services

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.pattern.ask
import akka.persistence.jdbc.query.scaladsl.JdbcReadJournal
import akka.persistence.query.PersistenceQuery
import akka.stream.scaladsl.Sink
import com.tosware.nkm.{NkmTimeouts, UserId}
import com.tosware.nkm.actors.User
import com.tosware.nkm.actors.User._
import com.tosware.nkm.models.{Credentials, RegisterRequest}
import slick.jdbc.JdbcBackend
import akka.util.ByteString
import com.tosware.nkm.serializers.NkmJsonProtocol

import scala.concurrent.{ExecutionContextExecutor, Future}
import spray.json._

object UserService {
  sealed trait Event
  case class LoggedIn(userId: UserId) extends Event
  case object InvalidCredentials extends Event
  case object InternalError extends Event
}


class UserService(implicit db: JdbcBackend.Database, system: ActorSystem)
  extends NkmTimeouts
    with NkmJsonProtocol
{
  import UserService._

  def authenticate(creds: Credentials): Event = {
    val userActor: ActorRef = system.actorOf(User.props(creds.email))
    aw(userActor ? CheckLogin(creds.password)) match {
      case LoginSuccess => LoggedIn(creds.email)
      case LoginFailure => InvalidCredentials
    }
  }

  def authenticateOauthGoogle(googleCredential: String): Event = {
    val uri = s"https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=$googleCredential"
    val request = HttpRequest(uri = uri)
    val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val jwtTokenFuture: Future[GoogleOauthJwtToken] = responseFuture.flatMap { response =>
      Unmarshal(response.entity).to[ByteString].map { byteString =>
        byteString.utf8String.parseJson.convertTo[GoogleOauthJwtToken]
      }
    }

    val result: Future[Event] = jwtTokenFuture.map { jwtToken =>
      val userActor: ActorRef = system.actorOf(User.props(jwtToken.email))
      aw(userActor ? OauthLogin()) match {
        case LoginSuccess => LoggedIn(jwtToken.email)
        case LoginFailure => InternalError
      }
    }.recover {
      case _: Throwable => InternalError
    }

    aw(result)
  }

  def register(request: RegisterRequest): RegisterEvent = {
    val readJournal: JdbcReadJournal = PersistenceQuery(system).readJournalFor[JdbcReadJournal](JdbcReadJournal.Identifier)
    val userActor: ActorRef = system.actorOf(User.props(request.email))

    val registerEmailsFuture = readJournal.
      currentEventsByTag(User.registerTag, 0)
      .map(_.event.asInstanceOf[RegisterSuccess].email)
      .runWith(Sink.seq[String])

    val oauthEmailsFuture = readJournal.
      currentEventsByTag(User.oauthRegisterTag, 0)
      .map(_.event.asInstanceOf[OauthRegisterSuccess].email)
      .runWith(Sink.seq[String])

    implicit val ec: scala.concurrent.ExecutionContext = system.dispatcher

    val allEmailsFuture = for {
      registerEmails <- registerEmailsFuture
      oauthEmails <- oauthEmailsFuture
    } yield registerEmails ++ oauthEmails

    val allEmails = aw(allEmailsFuture)
    val emailExists = allEmails.contains(request.email)

    if(emailExists) RegisterFailure
    else aw(userActor ? Register(request.password)).asInstanceOf[RegisterEvent]
  }
}
