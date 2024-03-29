package com.tosware.nkm.services

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.pattern.ask
import akka.persistence.jdbc.query.scaladsl.JdbcReadJournal
import akka.persistence.query.PersistenceQuery
import akka.stream.scaladsl.Sink
import akka.util.ByteString
import com.tosware.nkm.NkmTimeouts
import com.tosware.nkm.actors.User
import com.tosware.nkm.actors.User.*
import com.tosware.nkm.models.CommandResponse.CommandResponse
import com.tosware.nkm.models.NkmColor
import com.tosware.nkm.models.user.{UserSettings, UserState}
import com.tosware.nkm.serializers.NkmJsonProtocol
import com.tosware.nkm.services.http.routes.UserRequest
import spray.json.*

import scala.concurrent.{ExecutionContextExecutor, Future}

class UserService(implicit system: ActorSystem)
    extends NkmTimeouts
    with NkmJsonProtocol {

  private def getState(email: String): UserState = {
    val userActor: ActorRef = system.actorOf(User.props(email))
    aw(userActor ? GetState).asInstanceOf[UserState]
  }

  def authenticate(creds: UserRequest.Login): LoginEvent = {
    val userActor: ActorRef = system.actorOf(User.props(creds.email))
    aw(userActor ? CheckLogin(creds.password)).asInstanceOf[LoginEvent]
  }

  def authenticateOauthGoogle(googleCredential: String): LoginEvent = {
    val uri = s"https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=$googleCredential"
    val request = HttpRequest(uri = uri)
    val responseFuture: Future[HttpResponse] = Http().singleRequest(request)
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val jwtTokenFuture: Future[GoogleOauthJwtToken] = responseFuture.flatMap { response =>
      Unmarshal(response.entity).to[ByteString].map { byteString =>
        byteString.utf8String.parseJson.convertTo[GoogleOauthJwtToken]
      }
    }

    val result: Future[LoginEvent] = jwtTokenFuture.map { jwtToken =>
      val userActor: ActorRef = system.actorOf(User.props(jwtToken.email))
      aw(userActor ? OauthLogin()).asInstanceOf[LoginEvent]
    }.recover {
      case _: Throwable => User.LoginFailure("Internal error.")
    }

    aw(result)
  }

  def register(request: UserRequest.Register): RegisterEvent = {
    val readJournal: JdbcReadJournal =
      PersistenceQuery(system).readJournalFor[JdbcReadJournal](JdbcReadJournal.Identifier)
    val userActor: ActorRef = system.actorOf(User.props(request.email))

    val registerEmailsFuture = readJournal.currentEventsByTag(User.registerTag, 0)
      .map(_.event.asInstanceOf[RegisterSuccess].email)
      .runWith(Sink.seq[String])

    val oauthEmailsFuture = readJournal.currentEventsByTag(User.oauthRegisterTag, 0)
      .map(_.event.asInstanceOf[OauthRegisterSuccess].email)
      .runWith(Sink.seq[String])

    implicit val ec: scala.concurrent.ExecutionContext = system.dispatcher

    val allEmailsFuture = for {
      registerEmails <- registerEmailsFuture
      oauthEmails <- oauthEmailsFuture
    } yield registerEmails ++ oauthEmails

    val allEmails = aw(allEmailsFuture)
    val emailExists = allEmails.contains(request.email)

    if (emailExists) RegisterFailure
    else aw(userActor ? Register(request.password)).asInstanceOf[RegisterEvent]
  }

  def getUserSettings(email: String): UserSettings = {
    getState(email).userSettings
  }

  def setPreferredColor(email: String, colorNameOpt: Option[String]): Future[CommandResponse] = {
    val userActor: ActorRef = system.actorOf(User.props(email))

    val colorOpt = colorNameOpt.flatMap(colorName => NkmColor.availableColors.find(_.name == colorName))
    (userActor ? User.SetPreferredColor(colorOpt)).mapTo[CommandResponse]
  }

  def setLanguage(email: String, language: String): Future[CommandResponse] = {
    val userActor: ActorRef = system.actorOf(User.props(email))
    (userActor ? User.SetLanguage(language)).mapTo[CommandResponse]
  }

  def isColorAvailable(color: Option[String]): Boolean =
    color.fold(true)(NkmColor.availableColorNames.contains)

  def isLanguageAvailable(language: String): Boolean =
    Seq("English", "Polski").contains(language)
}
