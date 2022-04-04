package helpers

import akka.actor.ActorRef
import akka.pattern.ask
import com.tosware.NKM.DBManager
import com.tosware.NKM.actors.Lobby
import com.tosware.NKM.models.lobby.LobbyState
import com.tosware.NKM.models.lobby.ws.LobbyCreationRequest
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Await

trait LobbyApiTrait extends UserApiTrait
  {
    val lobbyName = "lobby_name"
    var lobbyId: String = ""
    override def beforeEach(): Unit = {
      super.beforeEach()

      Post("/api/create_lobby", LobbyCreationRequest(lobbyName)).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check {
        lobbyId = responseAs[String]
      }
    }

    // check actor and database integrity after each test
    // NOTE: afterEach() method is not used, as it is executed after the test, not within the test
    override def withFixture(test: NoArgTest) = {
      try super.withFixture(test)
      finally {
        checkActorAndDatabaseIntegrity()
      }
    }

    def checkActorAndDatabaseIntegrity(): Unit = {
      val lobbyActor: ActorRef = system.actorOf(Lobby.props(lobbyId))
      val lobbyStateFuture = (lobbyActor ? Lobby.GetState).mapTo[LobbyState]

      val q = DBManager.lobbies.filter(l => l.id === lobbyId).take(1).result.head
      val databaseLobbyStateFuture = db.run(q)

      val f = for {
        lobbyState <- lobbyStateFuture
        databaseLobbyState <- databaseLobbyStateFuture
      } yield {
        (lobbyState, databaseLobbyState)
      }

      val (lobbyState, databaseLobbyState) = Await.result(f, atMost)

      lobbyState shouldEqual databaseLobbyState
    }
  }
