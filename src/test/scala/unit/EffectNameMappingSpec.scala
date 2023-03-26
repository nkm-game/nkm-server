package unit

import com.tosware.nkm._
import com.tosware.nkm.models.game.effects.BlackBlood
import com.tosware.nkm.serializers.NkmJsonProtocol
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import spray.json._

class EffectNameMappingSpec
  extends AnyWordSpecLike
    with Matchers
    with Logging
    with NkmJsonProtocol
{
  "Effect names" must {
    "map" in {
      BlackBlood.metadata.id.toJson should be (BlackBlood.metadata.name.toJson)
    }
  }
}
