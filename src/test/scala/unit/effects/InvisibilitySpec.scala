package unit.effects

import com.tosware.nkm.*
import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.hecate.Aster
import com.tosware.nkm.models.game.abilities.roronoa_zoro.OgreCutter
import com.tosware.nkm.models.game.character.{AttackType, CharacterMetadata}
import com.tosware.nkm.models.game.effects.Invisibility
import com.tosware.nkm.models.game.hex.{HexCoordinates, TestHexMapName}
import helpers.{TestScenario, TestUtils}
import org.scalatest.wordspec.AnyWordSpecLike

class InvisibilitySpec
    extends AnyWordSpecLike
    with TestUtils {
  private val effectMetadata = Invisibility.metadata
  private val characterMetadata =
    CharacterMetadata
      .empty()
      .copy(
        initialAbilitiesMetadataIds = Seq(OgreCutter.metadata.id, Aster.metadata.id),
        attackType = AttackType.Melee,
      )
  private val s = TestScenario.generate(TestHexMapName.Spacey2v2, characterMetadata)
  private val eGs: GameState = s.gameState
    .teleportCharacter(s.p(0)(1).character.id, HexCoordinates(-2, 0)) // move this character out of the way
    .addEffect(s.defaultCharacter.id, Invisibility(randomUUID(), 2))
  private val enemyTurnGs: GameState = eGs.passTurn(s.defaultCharacter.id)

  private val friendView = enemyTurnGs.toView(Some(s.owners(0)))
  private val enemyView = enemyTurnGs.toView(Some(s.owners(1)))

  private val defaultEnemyOgreCutterId: AbilityId = s.defaultEnemy.state.abilities(0).id
  private val defaultEnemyAsterId: AbilityId = s.defaultEnemy.state.abilities(1).id

  effectMetadata.name.toString must {
    "hide parent in enemy basic movement validator" in {
      implicit val gameState: GameState = enemyTurnGs
      assertCommandSuccess {
        GameStateValidator()
          .validateBasicMoveCharacter(s.owners(1), CoordinateSeq((1, 0), (0, 0), (-1, 0)), s.defaultEnemy.id)
      }
    }
    "hide parent in enemy basic attack range" in {
      implicit val gameState: GameState = enemyTurnGs
      s.defaultEnemy.basicAttackTargets should not contain s.defaultCoordinates
      s.defaultEnemy.basicAttackTargets should contain(HexCoordinates(-2, 0))
    }
    "hide parent in enemy basic attack validator" in {
      implicit val gameState: GameState = enemyTurnGs
      assertCommandFailure {
        GameStateValidator()
          .validateBasicAttackCharacter(s.owners(1), s.defaultEnemy.id, s.defaultCharacter.id)
      }
    }
    "hide parent in enemy ability range" in {
      implicit val gameState: GameState = enemyTurnGs
      gameState.abilityById(defaultEnemyOgreCutterId).targetsInRange should not contain s.defaultCoordinates
    }

    "hide parent in enemy ability validator" in {
      implicit val gameState: GameState = enemyTurnGs
      assertCommandFailure {
        GameStateValidator()
          .validateAbilityUseOnCharacter(s.owners(1), s.defaultAbilityId, s.defaultCharacter.id)
      }
      gameState.abilityById(defaultEnemyOgreCutterId).targetsInRange should not contain s.defaultCoordinates
    }
    "hide parent identifying state" in {
      val friendCharacterView = friendView.characters.find(_.id == s.defaultCharacter.id).get
      val enemyCharacterView = enemyView.characters.find(_.id == s.defaultCharacter.id).get

      friendCharacterView.state should not be None
      friendCharacterView.basicAttackTargets should not be empty
      friendCharacterView.basicAttackCellCoords should not be empty

      enemyCharacterView.state should be(None)
      enemyCharacterView.basicAttackTargets should be(empty)
      enemyCharacterView.basicAttackCellCoords should be(empty)
    }

    "hide parent effects" in {
      friendView.effects.find(_.parentCharacterId == s.defaultCharacter.id) should not be None
      enemyView.effects.find(_.parentCharacterId == s.defaultCharacter.id) should be(None)
    }
    "hide parent abilities" in {
      friendView.abilities.find(_.parentCharacterId == s.defaultCharacter.id) should not be None
      enemyView.abilities.find(_.parentCharacterId == s.defaultCharacter.id) should be(None)
    }
    "hide parent position on map" in {
      friendView.hexMap.getCellOfCharacter(s.defaultCharacter.id) should not be None
      enemyView.hexMap.getCellOfCharacter(s.defaultCharacter.id) should be(None)
    }
    "interrupt enemy basic movement if enemy steps on them" in fail()
    "interrupt enemy move ability when they would be in range instead of target if enemy steps on them" in fail()
    "reveal parent if enemy steps on them" in fail()
    "reveal parent and hit it if enemy melee character tries to attack over them" in fail()
    "reveal parent on parent basic attack" in fail()
    "reveal parent on parent contact ability use" in fail()
    "not reveal parent on parent non-contact ability use" in fail()
  }
}
