package helpers

import akka.http.scaladsl.model.headers.RawHeader
import com.tosware.NKM.models.{Credentials, RegisterRequest}

trait UserApiTrait extends ApiTrait
  {
    val usernames = (1 to 3).map(x => { s"test_user_$x" })
    val registerRequests = usernames.map(u => RegisterRequest(u, s"test_user_$u@example.com", "password"))
    var tokens: List[String] = List()

    def getAuthHeader(token: String) = RawHeader("Authorization", s"Bearer $token")

    override def beforeEach(): Unit = {
      super.beforeEach()
      registerRequests.foreach(r => Post("/api/register", r) ~> routes)
      registerRequests.foreach(r => Post("/api/login", Credentials(r.login, r.password)) ~> routes ~> check {
        tokens = tokens :+ responseAs[String]
      })
    }
  }
