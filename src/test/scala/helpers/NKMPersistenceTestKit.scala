package helpers

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import com.tosware.NKM.actors.LobbiesManager
import com.tosware.NKM.services.{GameService, LobbyService, NKMDataService, UserService}
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration._
import scala.language.postfixOps

class NKMPersistenceTestKit (_system: ActorSystem) extends TestKit(_system)
  with NKMTestTrait
  with ImplicitSender
  with AnyWordSpecLike
{
  implicit val NKMDataService: NKMDataService = new NKMDataService()
  implicit val userService: UserService = new UserService()
  val lobbiesManagerActor: ActorRef = system.actorOf(LobbiesManager.props(NKMDataService))
  implicit val lobbyService: LobbyService = new LobbyService(lobbiesManagerActor)
  implicit val gameService: GameService = new GameService()
  def within2000[T](f: => T): T = within(2000 millis)(f)

  override def beforeAll(): Unit = {
    super.beforeAll()
  }
}
