package integration.api

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Route
import com.tosware.NKM.models._
import com.tosware.NKM.models.game.{HexMap, NKMCharacterMetadata}
import helpers.ApiTrait
import pdi.jwt.{JwtAlgorithm, JwtSprayJson}
import spray.json._

import scala.language.postfixOps
import scala.util.Success

class PublicDataSpec extends ApiTrait
{
  "API" must {
    "return hexmaps" in {
      Get("/api/maps") ~> Route.seal(routes) ~> check {
        status shouldEqual OK
        val hexmaps = responseAs[Seq[HexMap]]
        hexmaps.length should be > 1
      }
    }

    "return character metadatas" in {
      Get("/api/characters") ~> Route.seal(routes) ~> check {
        status shouldEqual OK
        val characters = responseAs[List[NKMCharacterMetadata]]
        characters.length should be > 1
      }
    }
  }
}
