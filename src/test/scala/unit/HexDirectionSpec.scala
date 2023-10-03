package unit

import com.tosware.nkm.*
import com.tosware.nkm.models.game.hex.HexDirection
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class HexDirectionSpec
    extends AnyWordSpecLike
    with Matchers
    with Logging {
  "HexDirection" must {
    "match right and left looks" in {
      HexDirection.values.map(_.lookLeft.lookRight) should be(HexDirection.values)
    }
  }
}
