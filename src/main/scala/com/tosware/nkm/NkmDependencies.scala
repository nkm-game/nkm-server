package com.tosware.nkm

import akka.actor.{ActorRef, ActorSystem}
import com.tosware.nkm.actors.ws.{GameSessionActor, LobbySessionActor}
import com.tosware.nkm.actors.{BugReportActor, GameIdTrackerActor}
import com.tosware.nkm.services.*
import com.tosware.nkm.services.http.directives.JwtSecretKey

import scala.concurrent.duration.*
import akka.pattern.gracefulStop

import scala.concurrent.Await
import scala.util.Random

class NkmDependencies(_system: ActorSystem) {
  implicit val system: ActorSystem = _system
  implicit val nkmDataService: NkmDataService = new NkmDataService()
  implicit val userService: UserService = new UserService()

  val gameIdTrackerActor: ActorRef =
    system.actorOf(GameIdTrackerActor.props(nkmDataService, userService), "game_id_tracker")
  val bugReportActor: ActorRef = system.actorOf(BugReportActor.props(), "bug_report")

  implicit val gameService: GameService = new GameService(gameIdTrackerActor)
  implicit val lobbyService: LobbyService = new LobbyService(gameIdTrackerActor)
  implicit val bugReportService: BugReportService = new BugReportService(bugReportActor)
  implicit val jwtSecretKey: JwtSecretKey =
    JwtSecretKey(sys.env.getOrElse("JWT_SECRET_KEY", "tmp_jwt_secret_key^*(^(*$#&(*"))

  implicit val lobbySessionActor: ActorRef =
    system.actorOf(LobbySessionActor.props(), s"lobby_session_${randomUUID()(new Random())}")
  implicit val gameSessionActor: ActorRef =
    system.actorOf(GameSessionActor.props(), s"game_session_${randomUUID()(new Random())}")

  def cleanup(): Unit = {
    val timeout = 5.seconds

    val stops = Seq(
      gracefulStop(gameIdTrackerActor, timeout),
      gracefulStop(bugReportActor, timeout),
      gracefulStop(lobbySessionActor, timeout),
      gracefulStop(gameSessionActor, timeout),
    )

    stops.foreach(stop => Await.result(stop, timeout))
  }
}
