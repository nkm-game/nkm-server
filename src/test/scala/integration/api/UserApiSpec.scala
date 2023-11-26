package integration.api

import akka.http.scaladsl.model.StatusCodes.*
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Route
import com.tosware.nkm.models.NkmColor
import com.tosware.nkm.models.user.UserSettings
import com.tosware.nkm.services.http.routes.UserRequest
import helpers.UserApiTrait

class UserApiSpec extends UserApiTrait {
  def fetchUserSettings(): UserSettings =
    Get("/api/user/settings/fetch")
      .addHeader(RawHeader("Authorization", s"Bearer ${tokens(0)}"))
      ~> Route.seal(routes) ~> check {
        status shouldEqual OK
        responseAs[UserSettings]
      }

  "User Settings API" must {
    "disallow non users to fetch settings" in {
      Get("/api/user/settings/fetch") ~> Route.seal(routes) ~> check {
        status shouldEqual Unauthorized
        responseAs[String] should be("Authentication is possible but has failed or not yet been provided.")
      }
    }

    "allow users to fetch settings" in {
      fetchUserSettings()
    }

    "allow users to set a preferred color" in {
      Post(
        "/api/user/settings/set_preferred_color",
        UserRequest.SetPreferredColor(Some(NkmColor.availableColorNames(3))),
      )
        .addHeader(RawHeader("Authorization", s"Bearer ${tokens(0)}")) ~> Route.seal(routes) ~> check {
        status shouldEqual NoContent
      }

      fetchUserSettings().preferredColor should be(Some(NkmColor.availableColors(3)))

      Post(
        "/api/user/settings/set_preferred_color",
        UserRequest.SetPreferredColor(None),
      )
        .addHeader(RawHeader("Authorization", s"Bearer ${tokens(0)}")) ~> Route.seal(routes) ~> check {
        status shouldEqual NoContent
      }

      fetchUserSettings().preferredColor should be(None)

    }

    "disallow users to set a preferred color that is not available" in {
      Post("/api/user/settings/set_preferred_color", UserRequest.SetPreferredColor(Some("invalidColor")))
        .addHeader(RawHeader("Authorization", s"Bearer ${tokens(0)}")) ~> Route.seal(routes) ~> check {
        status shouldEqual BadRequest
        responseAs[String] should include("Color not available")
      }
    }

    "disallow non users to set a preferred color" in {
      Post(
        "/api/user/settings/set_preferred_color",
        UserRequest.SetPreferredColor(Some(NkmColor.availableColorNames(0))),
      ) ~> Route.seal(routes) ~> check {
        status shouldEqual Unauthorized
      }
    }

    "allow users to set a language" in {
      Post("/api/user/settings/set_language", UserRequest.SetLanguage("English"))
        .addHeader(RawHeader("Authorization", s"Bearer ${tokens(0)}")) ~> Route.seal(routes) ~> check {
        status shouldEqual NoContent
      }
    }

    "disallow users to set a language that is not available" in {
      Post("/api/user/settings/set_language", UserRequest.SetLanguage("InvalidLanguage"))
        .addHeader(RawHeader("Authorization", s"Bearer ${tokens(0)}")) ~> Route.seal(routes) ~> check {
        status shouldEqual BadRequest
        responseAs[String] should include("Language not available")
      }
    }

    "disallow non users to set a language" in {
      Post("/api/user/settings/set_language", UserRequest.SetLanguage("English")) ~> Route.seal(routes) ~> check {
        status shouldEqual Unauthorized
      }
    }
  }
}
