package helpers

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.tosware.NKM.NKMDependencies
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration._
import scala.language.postfixOps

class NKMPersistenceTestKit (_system: ActorSystem) extends TestKit(_system)
  with NKMTestTrait
  with ImplicitSender
  with AnyWordSpecLike
{

  private var _depsOption: Option[NKMDependencies] = None
  def deps = _depsOption.get

  def within2000[T](f: => T): T = within(2000 millis)(f)

  override def beforeAll(): Unit = {
    super.beforeAll()
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    _depsOption = Some(new NKMDependencies(system, db))
  }

  override def afterEach(): Unit = {
    deps.cleanup()
    super.afterEach()
  }

}
