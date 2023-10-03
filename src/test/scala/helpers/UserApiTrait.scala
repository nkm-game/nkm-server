package helpers

import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.headers.RawHeader
import com.tosware.nkm.actors.User
import com.tosware.nkm.actors.User.GrantAdmin
import com.tosware.nkm.models.*

trait UserApiTrait extends ApiTrait {
  val numberOfUsers = 5
  val emails: Seq[String] = (0 until numberOfUsers).map(x => s"test_user_$x@example.com")
  val registerRequests: Seq[RegisterRequest] = emails.map(e => RegisterRequest(e, "password"))
  var tokens: Seq[String] = Seq()
  var userStates: Seq[UserStateView] = Seq()

  private val adminUserIndex = 4
  lazy val adminToken = tokens(adminUserIndex)

  def getAuthHeader(token: String) = RawHeader("Authorization", s"Bearer $token")

  def checkPostRequest[T](path: String, request: T, statusShouldBe: StatusCode)(implicit m: ToEntityMarshaller[T]) =
    Post(path, request) ~> routes ~> check(status shouldEqual statusShouldBe)
  def checkPostRequest[T](path: String, request: T, statusShouldBe: StatusCode, tokenNumber: Int)(implicit
      m: ToEntityMarshaller[T]
  ) =
    Post(path, request).addHeader(getAuthHeader(tokens(tokenNumber))) ~> routes ~> check(
      status shouldEqual statusShouldBe
    )

  override def beforeEach(): Unit = {
    super.beforeEach()
    registerRequests.foreach(r => Post("/api/register", r) ~> routes)

    val adminActor: ActorRef = system.actorOf(User.props(emails(adminUserIndex)))
    adminActor ! GrantAdmin

    registerRequests.foreach(r =>
      Post("/api/login", Credentials(r.email, r.password)) ~> routes ~> check {
        tokens = tokens :+ responseAs[AuthResponse].token
        userStates = userStates :+ responseAs[AuthResponse].userState
      }
    )
  }
}
