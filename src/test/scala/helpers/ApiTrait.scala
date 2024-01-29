package helpers

import akka.actor.ActorSystem
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import com.tosware.nkm.NkmDependencies
import com.tosware.nkm.services.http.HttpService

import scala.concurrent.duration.DurationInt

trait ApiTrait
    extends NkmIntegrationTestTrait
    with ScalatestRouteTest {
  private var _depsOption: Option[NkmDependencies] = None
  def deps = _depsOption.get
  private var _httpServiceOption: Option[HttpService] = None
  def httpService = _httpServiceOption.get
  def routes = httpService.routes

  implicit def default(implicit system: ActorSystem): RouteTestTimeout = RouteTestTimeout(5.seconds)

  override def beforeAll(): Unit =
    super.beforeAll()

  override def beforeEach(): Unit = {
    super.beforeEach()
    _depsOption = Some(new NkmDependencies(system))
    _httpServiceOption = Some(new HttpService(deps))
  }

  override def afterEach(): Unit = {
    // cleanup with timeout so the tests can finish
    deps.cleanup()

    // TODO: restart the actors to test if recovery works (below is not working)
    //    _depsOption = Some(new NkmDependencies(system))
    //    deps.gameService.getGameState("whatever")
    //    deps.cleanup()

    super.afterEach()
  }
}
