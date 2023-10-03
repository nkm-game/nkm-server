package helpers

import akka.http.scaladsl.testkit.WSProbe
import com.tosware.nkm.GameId
import com.tosware.nkm.models.game.event.GameEvent.GameEvent
import com.tosware.nkm.models.game.ws.GameResponseType
import com.tosware.nkm.serializers.NkmJsonProtocol
import org.scalatest.matchers.should.Matchers
import spray.json.*

import scala.concurrent.ExecutionContext.Implicits.*
import scala.concurrent.Future
import scala.util.*

class GameEventObserver(gameId: GameId, tokenId: Int)(wsTrait: WSTrait)
    extends NkmJsonProtocol
    with Matchers {
  import WSTrait.*
  private var isFutureRunning = true
  private var _observedEvents: Seq[GameEvent] = Seq()

  def observedEvents(): Seq[GameEvent] =
    _observedEvents

  def start(): Unit =
    wsTrait.withGameWS { implicit wsClient: WSProbe =>
      wsTrait.authG(tokenId)
      wsTrait.observeG(gameId).statusCode shouldBe ok

      Future {
        while (isFutureRunning) {
          val observedResponseOpt = Try {
            wsTrait.fetchResponseG()
          } match {
            case Success(value) => Some(value)
            case Failure(_)     => None
          }
          observedResponseOpt.foreach { observedResponse =>
            observedResponse.statusCode shouldBe ok
            observedResponse.gameResponseType shouldBe GameResponseType.Event
            val event = observedResponse.body.parseJson.convertTo[GameEvent]
            _observedEvents = _observedEvents :+ event
          }
        }
      }
    }

  def stop(): Unit =
    isFutureRunning = false

}
