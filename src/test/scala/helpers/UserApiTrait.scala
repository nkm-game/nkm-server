package helpers

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.headers.RawHeader
import com.tosware.NKM.models.{Credentials, RegisterRequest}

trait UserApiTrait extends ApiTrait
  {
    val usernames = (1 to 5).map(x => { s"test_user_$x" })
    val registerRequests = usernames.map(u => RegisterRequest(u, s"test_user_$u@example.com", "password"))
    var tokens: Seq[String] = Seq()

    def getAuthHeader(token: String) = RawHeader("Authorization", s"Bearer $token")

    def checkPostRequest[T](path: String, request: T, statusShouldBe: StatusCode)(implicit m: ToEntityMarshaller[T]) =
      Post(path, request) ~> routes ~> check(status shouldEqual statusShouldBe)
    def checkPostRequest[T](path: String, request: T, statusShouldBe: StatusCode, tokenNumber: Int)(implicit m: ToEntityMarshaller[T]) =
      Post(path, request).addHeader(getAuthHeader(tokens(tokenNumber))) ~> routes ~> check(status shouldEqual statusShouldBe)


    override def beforeEach(): Unit = {
      super.beforeEach()
      registerRequests.foreach(r => Post("/api/register", r) ~> routes)
      registerRequests.foreach(r => Post("/api/login", Credentials(r.login, r.password)) ~> routes ~> check {
        tokens = tokens :+ responseAs[String]
      })
    }
  }
