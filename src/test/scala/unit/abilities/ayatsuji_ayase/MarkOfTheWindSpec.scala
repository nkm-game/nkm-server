package unit.abilities.ayatsuji_ayase

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.ayatsuji_ayase.{CrackTheSky, MarkOfTheWind}
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.event.GameEvent
import com.tosware.nkm.models.game.hex.HexCoordinates
import helpers.{TestUtils, scenarios}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import spray.json._

class MarkOfTheWindSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  private val metadata = CharacterMetadata.empty()
    .copy(initialAbilitiesMetadataIds = Seq(
      MarkOfTheWind.metadata.id,
      CrackTheSky.metadata.id,
    ))
  private val s = scenarios.Simple1v1TestScenario(metadata)
  private implicit val gameState: GameState = s.gameState
  private val markAbilityId =
    s.characters.p0.state.abilities(0).id
  private val crackAbilityId =
    s.characters.p0.state.abilities(1).id

  private val markGs: GameState = gameState
    .useAbilityOnCoordinates(markAbilityId, HexCoordinates(0, 0))
    .endTurn()
    .passTurn(s.characters.p1.id)

  private val crackGs: GameState = markGs
    .useAbility(crackAbilityId, UseData(CoordinateSeq((0, 0)).toJson.toString))
    .endTurn()
    .passTurn(s.characters.p1.id)

  private val doubleMarkGs: GameState = gameState
    .useAbilityOnCoordinates(markAbilityId, HexCoordinates(1, 0))
    .endTurn()
    .passTurn(s.characters.p1.id)

  private val doubleCrackGs: GameState = doubleMarkGs
    .useAbility(crackAbilityId, UseData(CoordinateSeq((0, 0), (1, 0)).toJson.toString))
    .endTurn()
    .passTurn(s.characters.p1.id)

  MarkOfTheWind.metadata.name must {
    "be able to set up traps" in {
      assertCommandSuccess {
        GameStateValidator()(gameState)
          .validateAbilityUseOnCoordinates(s.characters.p0.owner.id, markAbilityId, HexCoordinates(0, 0))
      }

      assertCommandSuccess {
        GameStateValidator()(crackGs)
          .validateAbilityUseOnCoordinates(s.characters.p0.owner.id, markAbilityId, HexCoordinates(0, 0))
      }
    }

    "not be able to set up traps on the same tile" in {
      assertCommandFailure {
        GameStateValidator()(markGs)
          .validateAbilityUseOnCoordinates(s.characters.p0.owner.id, markAbilityId, HexCoordinates(0, 0))
      }
    }

    "not be able to set up traps outside map" in {
      assertCommandFailure {
        GameStateValidator()(gameState)
          .validateAbilityUseOnCoordinates(s.characters.p0.owner.id, markAbilityId, HexCoordinates(-10, 0))
      }
    }


    "be able to detonate traps" in {
      assertCommandSuccess {
        GameStateValidator()(markGs)
          .validateAbilityUse(s.characters.p0.owner.id, crackAbilityId, UseData(CoordinateSeq((0, 0)).toJson.toString))
      }

      assertCommandSuccess {
        GameStateValidator()(doubleMarkGs)
          .validateAbilityUse(s.characters.p0.owner.id, crackAbilityId, UseData(CoordinateSeq((0, 0)).toJson.toString))
      }

      assertCommandSuccess {
        GameStateValidator()(doubleMarkGs)
          .validateAbilityUse(s.characters.p0.owner.id, crackAbilityId, UseData(CoordinateSeq((1, 0)).toJson.toString))
      }

      assertCommandSuccess {
        GameStateValidator()(doubleMarkGs)
          .validateAbilityUse(s.characters.p0.owner.id, crackAbilityId, UseData(CoordinateSeq((0, 0), (1, 0)).toJson.toString))
      }
    }

    "not be able to detonate non existent traps" in {
      assertCommandFailure {
        GameStateValidator()(markGs)
          .validateAbilityUse(s.characters.p0.owner.id, crackAbilityId, UseData(CoordinateSeq((0, 0), (1, 0)).toJson.toString))
      }

      assertCommandFailure {
        GameStateValidator()(markGs)
          .validateAbilityUse(s.characters.p0.owner.id, crackAbilityId, UseData(CoordinateSeq((1, 0)).toJson.toString))
      }

      assertCommandFailure {
        GameStateValidator()(markGs)
          .validateAbilityUse(s.characters.p0.owner.id, crackAbilityId, UseData(CoordinateSeq((0, 0), (-1000, 0)).toJson.toString))
      }

      assertCommandFailure {
        GameStateValidator()(markGs)
          .validateAbilityUse(s.characters.p0.owner.id, crackAbilityId, UseData(CoordinateSeq((-1000, 0)).toJson.toString))
      }
    }


    "be able to deal damage by detonating selected trap" in {
      HexCoordinates(0, 0).toCell(markGs).effects.size should be > 0
      HexCoordinates(0, 0).toCell(crackGs).effects.size should be (0)
      crackGs.gameLog.events.ofType[GameEvent.CharacterDamaged].causedBy(crackAbilityId).size should be (1)
    }

    "be able to deal damage by detonating several selected traps" in {
      HexCoordinates(0, 0).toCell(doubleMarkGs).effects.size should be > 0
      HexCoordinates(1, 0).toCell(doubleMarkGs).effects.size should be > 0

      HexCoordinates(0, 0).toCell(doubleCrackGs).effects.size should be (0)
      HexCoordinates(1, 0).toCell(doubleCrackGs).effects.size should be > 0

      crackGs.gameLog.events.ofType[GameEvent.CharacterDamaged].causedBy(crackAbilityId).size should be (2)
    }

    "delete first trap if set above the limit" in {
      // TODO: increase test map size
      fail()
    }

    "hide the traps from other players" in {
      HexCoordinates(0, 0)
        .toCell(markGs)
        .toView(Some(s.characters.p0.id))
        .effects.size should be (1)

      HexCoordinates(0, 0)
        .toCell(markGs)
        .toView(Some(s.characters.p1.id))
        .effects.size should be (0)

      markGs.gameLog.toView(Some(s.characters.p0.id))
        .events.ofType[GameEvent.EffectAddedToCell].size should be (1)

      markGs.gameLog.toView(Some(s.characters.p1.id))
        .events.ofType[GameEvent.EffectAddedToCell].size should be (0)
    }
  }
}