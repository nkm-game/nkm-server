package com.tosware.NKM

import akka.actor.{ActorRef, ActorSystem}
import com.tosware.NKM.actors.ws.{GameSessionActor, LobbySessionActor}
import com.tosware.NKM.actors.{GamesManager, LobbiesManager}
import com.tosware.NKM.services.{GameService, LobbyService, NKMDataService, UserService}
import com.tosware.NKM.services.http.directives.JwtSecretKey
import slick.jdbc.JdbcBackend

class NKMDependencies(_system: ActorSystem, _db: JdbcBackend.Database) {
  implicit val system: ActorSystem = _system
  implicit val db: JdbcBackend.Database = _db
  implicit val NKMDataService: NKMDataService = new NKMDataService()
  val gamesManagerActor: ActorRef = system.actorOf(GamesManager.props(NKMDataService), "games_manager")
  val lobbiesManagerActor: ActorRef = system.actorOf(LobbiesManager.props(NKMDataService), "lobbies_manager")
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
