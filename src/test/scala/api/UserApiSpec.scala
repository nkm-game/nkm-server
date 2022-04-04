package api

import akka.http.scaladsl.model.StatusCodes._
import com.tosware.NKM.models.lobby._
import com.tosware.NKM.models.lobby.ws.LobbyCreationRequest
import helpers.UserApiTrait

import scala.language.postfixOps

class UserApiSpec extends UserApiTrait
{
  "API" must {
    "allow creating lobby" in {
      Post("/api/create_lobby", LobbyCreationRequest("lobby_name")).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check {
        status shouldEqual Created
      }
    }

    "disallow creating lobby with wrong token" in {
      val token: String = "random_token"
      Post("/api/create_lobby", LobbyCreationRequest("lobby_name")).addHeader(getAuthHeader(token)) ~> routes ~> check {
        status shouldEqual Unauthorized
      }
    }

    "allow getting created lobbies" in {
      Get("/api/lobbies") ~> routes ~> check {
        status shouldEqual OK
        val lobbies = responseAs[Seq[LobbyState]]
        lobbies.length shouldEqual 0
      }

      Post("/api/create_lobby", LobbyCreationRequest("lobby_name")).addHeader(getAuthHeader(tokens(0))) ~> routes ~> check {
        status shouldEqual Created
      }

      Get("/api/lobbies") ~> routes ~> check {
        status shouldEqual OK
        val lobbies = responseAs[Seq[LobbyState]]
        lobbies.length shouldEqual 1
      }
    }
  }
}
