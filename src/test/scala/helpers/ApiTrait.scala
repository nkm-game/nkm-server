package helpers

import akka.actor.ActorSystem
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import com.tosware.NKM.NKMDependencies
import com.tosware.NKM.services.http.HttpService

import scala.concurrent.duration.DurationInt

trait ApiTrait
    extends NKMIntegrationTestTrait
      with ScalatestRouteTest
  {
    private var _depsOption: Option[NKMDependencies] = None
    def deps = _depsOption.get
    private var _httpServiceOption: Option[HttpService] = None
    def httpService = _httpServiceOption.get
    def routes = httpService.routes

    implicit def default(implicit system: ActorSystem): RouteTestTimeout = RouteTestTimeout(5.seconds)

    override def beforeAll(): Unit = {
      super.beforeAll()
    }

    override def beforeEach(): Unit = {
      super.beforeEach()
      _depsOption = Some(new NKMDependencies(system, db))
      _httpServiceOption = Some(new HttpService(deps))
    }

    override def afterEach(): Unit = {
      deps.cleanup()
      super.afterEach()
    }
}
