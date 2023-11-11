package integration.api

import akka.http.scaladsl.model.StatusCodes.*
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.FromResponseUnmarshaller
import com.tosware.nkm.models.game.ability.AbilityMetadata
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.character_effect.CharacterEffectMetadata
import com.tosware.nkm.models.game.hex.HexMapTemplate
import com.tosware.nkm.models.game.hex_effect.HexCellEffectMetadata
import helpers.{ApiTrait, NotWorkingOnCI}

import scala.reflect.ClassTag

class PublicDataSpec extends ApiTrait {
  "API" must {
    def assertDataExists[B <: Iterable[?]: FromResponseUnmarshaller: ClassTag](route: String) =
      Get(route) ~> Route.seal(routes) ~> check {
        status shouldEqual OK
        val data = responseAs[B]
        data should not be empty
      }

    "return hexmaps" in
      assertDataExists[Seq[HexMapTemplate]]("/api/maps")

    "return character metadatas" in
      assertDataExists[Seq[CharacterMetadata]]("/api/characters")

    "return ability metadatas" in
      assertDataExists[Seq[AbilityMetadata]]("/api/abilities")

    "return character effect metadatas" in
      assertDataExists[Seq[CharacterEffectMetadata]]("/api/character_effects")

    "return hex cell effect metadatas" in
      assertDataExists[Seq[HexCellEffectMetadata]]("/api/hex_cell_effects")

    "return current version" taggedAs NotWorkingOnCI in {
      Get("/api/version") ~> Route.seal(routes) ~> check {
        status shouldEqual OK
      }
    }
  }
}
