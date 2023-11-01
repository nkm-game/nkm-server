package helpers

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.tosware.nkm.NkmDependencies
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration.*
import scala.language.postfixOps

class NkmPersistenceTestKit(_system: ActorSystem) extends TestKit(_system)
    with NkmIntegrationTestTrait
    with ImplicitSender
    with AnyWordSpecLike {

  private var _depsOption: Option[NkmDependencies] = None
  def deps = _depsOption.get

  def within2000[T](f: => T): T = within(2000 millis)(f)

  override def beforeAll(): Unit =
    super.beforeAll()

  override def beforeEach(): Unit = {
    super.beforeEach()
    _depsOption = Some(new NkmDependencies(system))
  }

  override def afterEach(): Unit = {
    deps.cleanup()
    super.afterEach()
  }

}
