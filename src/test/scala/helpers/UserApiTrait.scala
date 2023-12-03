package helpers

import akka.actor.ActorRef
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.RawHeader
import com.tosware.nkm.actors.User
import com.tosware.nkm.actors.User.GrantAdmin
import com.tosware.nkm.models.user.UserStateView
import com.tosware.nkm.models.user.response.AuthResponse
import com.tosware.nkm.services.http.routes.UserRequest

trait UserApiTrait extends ApiTrait {
  val numberOfUsers = 5
  val emails: Seq[String] = (0 until numberOfUsers).map(x => s"test_user_$x@example.com")
  val registerRequests: Seq[UserRequest.Register] = emails.map(e => UserRequest.Register(e, "password"))
  var tokens: Seq[String] = Seq()
  var userStates: Seq[UserStateView] = Seq()

  private val adminUserIndex = 4
  lazy val adminToken = tokens(adminUserIndex)

  def getAuthHeader(token: String): RawHeader = RawHeader("Authorization", s"Bearer $token")

  implicit class HttpRequestExtensions(httpRequest: HttpRequest) {
    def addAuthHeader(token: String): HttpRequest = httpRequest.addHeader(getAuthHeader(token))
    def addAuthHeader(token: Int): HttpRequest = addAuthHeader(tokens(token))
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    registerRequests.foreach(r => Post("/api/register", r) ~> routes)

    val adminActor: ActorRef = system.actorOf(User.props(emails(adminUserIndex)))
    adminActor ! GrantAdmin

    registerRequests.foreach(r =>
      Post("/api/login", UserRequest.Login(r.email, r.password)) ~> routes ~> check {
        tokens = tokens :+ responseAs[AuthResponse].token
        userStates = userStates :+ responseAs[AuthResponse].userState
      }
    )
  }
}
