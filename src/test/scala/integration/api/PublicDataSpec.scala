package integration.api

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.FromResponseUnmarshaller
import com.tosware.nkm.models.game.ability.AbilityMetadata
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.hex.{HexCell, HexMap}
import com.tosware.nkm.models.game.character_effect.CharacterEffectMetadata
import helpers.ApiTrait

import scala.reflect.ClassTag

class PublicDataSpec extends ApiTrait
{
  "API" must {
    def assertDataExists[B <: Iterable[_]: FromResponseUnmarshaller: ClassTag](route: String) = {
      Get(route) ~> Route.seal(routes) ~> check {
        status shouldEqual OK
        val data = responseAs[B]
        data.size should be > 0
      }
    }

    "return hexmaps" in
      assertDataExists[Seq[HexMap[HexCell]]]("/api/maps")

    "return character metadatas" in
      assertDataExists[Seq[CharacterMetadata]]("/api/characters")

    "return ability metadatas" in
      assertDataExists[Seq[AbilityMetadata]]("/api/abilities")

    "return character effect metadatas" in
      assertDataExists[Seq[CharacterEffectMetadata]]("/api/character_effects")

    "return current version" in {
      Get("/api/version") ~> Route.seal(routes) ~> check {
        status shouldEqual OK
      }
    }
  }
}
