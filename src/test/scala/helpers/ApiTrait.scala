package helpers

import akka.actor.ActorSystem
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import com.tosware.NKM.services.http.HttpService
import com.tosware.NKM.services.{GameService, LobbyService, NKMDataService, UserService}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.duration.DurationInt

trait ApiTrait
    extends NKMTestTrait
      with ScalatestRouteTest
      with HttpService
  {
    implicit val NKMDataService: NKMDataService = new NKMDataService()
    implicit val userService: UserService = new UserService()
    implicit val lobbyService: LobbyService = new LobbyService()
    implicit val gameService: GameService = new GameService()
    implicit def default(implicit system: ActorSystem): RouteTestTimeout = RouteTestTimeout(5.seconds)
    val logger: Logger = LoggerFactory.getLogger(getClass)

    override def beforeAll(): Unit = {
      super.beforeAll()
    }
    override def afterAll(): Unit = {
      super.afterAll()
    }
}
