package api

import akka.http.scaladsl.model.headers.RawHeader
import com.tosware.NKM.models.{Credentials, RegisterRequest}

trait UserApiTrait extends ApiTrait
  {
    val username = "test"
    val email = "test@example.com"
    val password = "password"
    var token: String = ""

    def getAuthHeader(token: String) = RawHeader("Authorization", s"Bearer $token")

    override def beforeEach(): Unit = {
      super.beforeEach()
      Post("/api/register", RegisterRequest(username, email, password)) ~> routes
      Post("/api/login", Credentials(username, password)) ~> routes ~> check {
        token = responseAs[String]
      }
    }
  }
