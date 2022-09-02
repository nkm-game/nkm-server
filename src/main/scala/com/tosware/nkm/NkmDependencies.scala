package com.tosware.nkm

import akka.actor.{ActorRef, ActorSystem}
import com.tosware.nkm.actors.ws.{GameSessionActor, LobbySessionActor}
import com.tosware.nkm.actors.{GamesManager, LobbiesManager}
import com.tosware.nkm.services.{GameService, LobbyService, NkmDataService, UserService}
import com.tosware.nkm.services.http.directives.JwtSecretKey
import slick.jdbc.JdbcBackend

class NkmDependencies(_system: ActorSystem, _db: JdbcBackend.Database) {
  implicit val system: ActorSystem = _system
  implicit val db: JdbcBackend.Database = _db
  implicit val nkmDataService: NkmDataService = new NkmDataService()
  val gamesManagerActor: ActorRef = system.actorOf(GamesManager.props(nkmDataService), "games_manager")
  val lobbiesManagerActor: ActorRef = system.actorOf(LobbiesManager.props(nkmDataService), "lobbies_manager")
  implicit val userService: UserService = new UserService()
  implicit val gameService: GameService = new GameService(gamesManagerActor)
  implicit val lobbyService: LobbyService = new LobbyService(lobbiesManagerActor)
  implicit val jwtSecretKey: JwtSecretKey = JwtSecretKey(sys.env.getOrElse("JWT_SECRET_KEY", "tmp_jwt_secret_key^*(^(*$#&(*"))

  implicit val lobbySessionActor: ActorRef = system.actorOf(LobbySessionActor.props(), "lobby_session")
  implicit val gameSessionActor: ActorRef = system.actorOf(GameSessionActor.props(), "game_session")

  def cleanup(): Unit = {
    system.stop(gamesManagerActor)
    system.stop(lobbiesManagerActor)
    system.stop(lobbySessionActor)
    system.stop(gameSessionActor)
  }
}