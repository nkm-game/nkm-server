package unit.abilities.ayatsuji_ayase

import com.tosware.nkm.*
import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.ayatsuji_ayase.{CrackTheSky, MarkOfTheWind}
import com.tosware.nkm.models.game.ability.UseData
import com.tosware.nkm.models.game.ability.UseData.HexCoordinatesMarker
import com.tosware.nkm.models.game.character.CharacterMetadata
import com.tosware.nkm.models.game.event.GameEvent
import com.tosware.nkm.models.game.hex.{HexCoordinates, TestHexMapName}
import helpers.{TestScenario, TestUtils}

class MarkOfTheWindSpec extends TestUtils {
  private val metadata = CharacterMetadata.empty()
    .copy(initialAbilitiesMetadataIds =
      Seq(
        MarkOfTheWind.metadata.id,
        CrackTheSky.metadata.id,
      )
    )
  private val s = TestScenario.generate(TestHexMapName.Spacey1v1, metadata)
  private val gameState: GameState = s.gameState
  private val markAbilityId =
    s.defaultAbilityId
  private val crackAbilityId =
    s.defaultCharacter.state.abilities(1).id

  private val markGs: GameState = gameState
    .useAbility(markAbilityId, UseData(HexCoordinates(0, 0)))
    .passAllCharactersInCurrentPhase()

  private val crackGs: GameState = markGs
    .useAbility(crackAbilityId, UseData(CoordinateSeq((0, 0))))
    .passAllCharactersInCurrentPhase()

  private val doubleMarkGs: GameState = markGs
    .useAbility(markAbilityId, UseData(HexCoordinates(1, 0)))
    .passAllCharactersInCurrentPhase()

  private val doubleCrackGs: GameState = doubleMarkGs
    .useAbility(crackAbilityId, UseData(CoordinateSeq((0, 0), (1, 0))))
    .passAllCharactersInCurrentPhase()

  private val fiveMarkGs: GameState = doubleMarkGs
    .useAbility(markAbilityId, UseData(HexCoordinates(2, 0))).endTurn().passTurn(s.defaultEnemy.id)
    .useAbility(markAbilityId, UseData(HexCoordinates(3, 0))).endTurn().passTurn(s.defaultEnemy.id)
    .useAbility(markAbilityId, UseData(HexCoordinates(4, 0))).endTurn().passTurn(s.defaultEnemy.id)

  private val sixMarkGs: GameState = fiveMarkGs
    .useAbility(markAbilityId, UseData(HexCoordinates(5, 0))).endTurn().passTurn(s.defaultEnemy.id)

  MarkOfTheWind.metadata.name must {
    "be able to set up traps" in {
      assertCommandSuccess {
        GameStateValidator()(gameState)
          .validateAbilityUse(s.owners(0), markAbilityId, UseData(HexCoordinates(0, 0)))
      }

      assertCommandSuccess {
        GameStateValidator()(crackGs)
          .validateAbilityUse(s.owners(0), markAbilityId, UseData(HexCoordinates(0, 0)))
      }
    }

    "not be able to set up traps on the same tile" in {
      assertCommandFailure {
        GameStateValidator()(markGs)
          .validateAbilityUse(s.owners(0), markAbilityId, UseData(HexCoordinates(0, 0)))
      }
    }

    "not be able to set up traps outside map" in {
      assertCommandFailure {
        GameStateValidator()(gameState)
          .validateAbilityUse(s.owners(0), markAbilityId, UseData(HexCoordinates(-10, 0)))
      }
    }

    "be able to detonate traps" in {
      assertCommandSuccess {
        GameStateValidator()(markGs)
          .validateAbilityUse(s.owners(0), crackAbilityId, UseData(CoordinateSeq((0, 0))))
      }

      assertCommandSuccess {
        GameStateValidator()(doubleMarkGs)
          .validateAbilityUse(s.owners(0), crackAbilityId, UseData(CoordinateSeq((0, 0))))
      }

      assertCommandSuccess {
        GameStateValidator()(doubleMarkGs)
          .validateAbilityUse(s.owners(0), crackAbilityId, UseData(CoordinateSeq((1, 0))))
      }

      assertCommandSuccess {
        GameStateValidator()(doubleMarkGs)
          .validateAbilityUse(s.owners(0), crackAbilityId, UseData(CoordinateSeq((0, 0), (1, 0))))
      }
    }

    "not be able to detonate non existent traps" in {
      assertCommandFailure {
        GameStateValidator()(markGs)
          .validateAbilityUse(s.owners(0), crackAbilityId, UseData(CoordinateSeq((0, 0), (1, 0))))
      }

      assertCommandFailure {
        GameStateValidator()(markGs)
          .validateAbilityUse(s.owners(0), crackAbilityId, UseData(CoordinateSeq((1, 0))))
      }

      assertCommandFailure {
        GameStateValidator()(markGs)
          .validateAbilityUse(s.owners(0), crackAbilityId, UseData(CoordinateSeq((0, 0), (-1000, 0))))
      }

      assertCommandFailure {
        GameStateValidator()(markGs)
          .validateAbilityUse(s.owners(0), crackAbilityId, UseData(CoordinateSeq((-1000, 0))))
      }
    }

    "be able to deal damage by detonating selected trap" in {
      HexCoordinates(0, 0).toCell(markGs).effects.size should be(1)
      HexCoordinates(0, 0).toCell(crackGs).effects.size should be(0)
      crackGs.gameLog.events.ofType[GameEvent.CharacterDamaged].causedBy(crackAbilityId).size should be(1)
    }

    "be able to deal damage by detonating several selected traps" in {
      HexCoordinates(0, 0).toCell(doubleMarkGs).effects.size should be(1)
      HexCoordinates(1, 0).toCell(doubleMarkGs).effects.size should be(1)

      HexCoordinates(0, 0).toCell(doubleCrackGs).effects.size should be(0)
      HexCoordinates(1, 0).toCell(doubleCrackGs).effects.size should be(0)

      doubleCrackGs.gameLog.events.ofType[GameEvent.CharacterDamaged].causedBy(crackAbilityId).size should be(2)
    }

    "delete first trap if set above the limit" in {
      HexCoordinates(0, 0).toCell(fiveMarkGs).effects.size should be(1)
      HexCoordinates(0, 0).toCell(sixMarkGs).effects.size should be(0)
    }

    "hide the traps from other players" in {
      HexCoordinates(0, 0)
        .toCell(markGs)
        .toView(Some(s.owners(0)))(markGs)
        .effects should not be empty

      HexCoordinates(0, 0)
        .toCell(markGs)
        .toView(Some(s.owners(1)))(markGs)
        .effects should be(empty)

      markGs.gameLog.toView(Some(s.owners(0)))(markGs)
        .events.ofType[GameEvent.EffectAddedToCell].size should be(1)

      markGs.gameLog.toView(Some(s.owners(1)))(markGs)
        .events.ofType[GameEvent.EffectAddedToCell].size should be(0)
    }
  }
}
