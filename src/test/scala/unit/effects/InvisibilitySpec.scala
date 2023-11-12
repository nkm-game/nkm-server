package unit.effects

import com.tosware.nkm.*
import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.hecate.Aster
import com.tosware.nkm.models.game.abilities.ryuko_matoi.FiberDecapitation
import com.tosware.nkm.models.game.character.{AttackType, CharacterMetadata}
import com.tosware.nkm.models.game.effects.Invisibility
import com.tosware.nkm.models.game.event.GameEvent
import com.tosware.nkm.models.game.hex.{HexCoordinates, TestHexMapName}
import helpers.{TestScenario, TestUtils}
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.Checkpoints.Checkpoint

class InvisibilitySpec
    extends AnyWordSpecLike
    with TestUtils {
  private val effectMetadata = Invisibility.metadata
  private val characterMetadata =
    CharacterMetadata
      .empty()
      .copy(
        initialAbilitiesMetadataIds = Seq(FiberDecapitation.metadata.id, Aster.metadata.id),
        attackType = AttackType.Melee,
      )
  private val s = TestScenario.generate(TestHexMapName.Spacey2v2, characterMetadata)
  private val eGs: GameState = s.gameState
    .teleportCharacter(s.p(0)(1).character.id, HexCoordinates(-2, 0)) // move this character out of the way
    .addEffect(s.defaultCharacter.id, Invisibility(randomUUID(), 2))
  private val enemyTurnGs: GameState = eGs.passTurn(s.defaultCharacter.id)
  private val enemyTurnCollisionGs: GameState =
    enemyTurnGs
      .teleportCharacter(s.p(0)(1).character.id, HexCoordinates(-4, 0))
      .teleportCharacter(s.defaultCharacter.id, HexCoordinates(-2, 0))

  private val friendView = enemyTurnGs.toView(Some(s.owners(0)))
  private val enemyView = enemyTurnGs.toView(Some(s.owners(1)))

  private val defaultCharacterContactAbilityId: AbilityId = s.defaultCharacter.state.abilities(0).id
  private val defaultCharacterAsterId: AbilityId = s.defaultCharacter.state.abilities(1).id

  private val defaultEnemyContactAbilityId: AbilityId = s.defaultEnemy.state.abilities(0).id

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
      gameState.abilityById(defaultEnemyContactAbilityId).targetsInRange should not contain s.defaultCoordinates
    }
    "hide parent in enemy ability validator" in {
      implicit val gameState: GameState = enemyTurnGs
      assertCommandFailure {
        GameStateValidator()
          .validateAbilityUseOnCharacter(s.owners(1), defaultEnemyContactAbilityId, s.defaultCharacter.id)
      }
      assertCommandSuccess {
        GameStateValidator()
          .validateAbilityUseOnCharacter(s.owners(1), defaultEnemyContactAbilityId, s.p(0)(1).character.id)
      }
      gameState.abilityById(defaultEnemyContactAbilityId).targetsInRange should not contain s.defaultCoordinates
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
    "interrupt enemy basic movement if enemy steps on them" in {
      val interruptGs =
        enemyTurnCollisionGs.basicMoveCharacter(s.defaultEnemy.id, CoordinateSeq((0, 0), (-1, 0), (-2, 0), (-3, 0)))

      s.defaultEnemy.parentCellOpt(interruptGs).get.coordinates.toTuple should be(-1, 0)
    }
    "reveal parent when enemy steps on them" in {
      val interruptGs =
        enemyTurnCollisionGs.basicMoveCharacter(s.defaultEnemy.id, CoordinateSeq((0, 0), (-1, 0), (-2, 0), (-3, 0)))

      assertEffectDoesNotExistsOfType[effects.Invisibility](s.defaultCharacter.id)(interruptGs)

      s.defaultEnemy.parentCellOpt(interruptGs).map(_.coordinates) should be(Some(HexCoordinates(-1, 0)))
      s.defaultCharacter.parentCellOpt(interruptGs).map(_.coordinates) should be(Some(HexCoordinates(-2, 0)))
    }
    "reveal and push parent when enemy teleports on them" in {
      val setupTpGs = enemyTurnGs
        .teleportCharacter(s.defaultCharacter.id, HexCoordinates(-4, 0))
        .teleportCharacter(s.p(0)(1).character.id, HexCoordinates(-1, 0))
      val tpGs = setupTpGs.useAbilityOnCharacter(defaultEnemyContactAbilityId, s.p(0)(1).character.id)

      val cp = new Checkpoint
      cp(assertEffectDoesNotExistsOfType[effects.Invisibility](s.defaultCharacter.id)(tpGs))
      cp(s.defaultEnemy.parentCellOpt(tpGs).map(_.coordinates) should be(Some(HexCoordinates(-4, 0))))
      cp(s.defaultCharacter.parentCellOpt(tpGs).map(_.coordinates) should not be Some(HexCoordinates(-4, 0)))
      cp.reportAll()
    }
    // not sure if it should work this way, commented for now
//    "reveal parent and hit it on collision with enemy move ability" in {
//      val aInterruptGs =
//        enemyTurnCollisionGs.useAbilityOnCharacter(defaultEnemyContactAbilityId, s.p(0)(1).character.id)
//
//      assertEffectDoesNotExistsOfType[effects.Invisibility](s.defaultCharacter.id)(aInterruptGs)
//      aInterruptGs.gameLog.events
//        .ofType[GameEvent.CharacterDamaged]
//        .ofCharacter(s.defaultCharacter.id) should not be empty
//    }
    "reveal parent and hit it when enemy melee character tries to attack over them" in {
      val attackedOverGs = enemyTurnGs.basicAttack(s.defaultEnemy.id, s.p(0)(1).character.id)

      assertEffectDoesNotExistsOfType[effects.Invisibility](s.defaultCharacter.id)(attackedOverGs)

      val basicAttackEvents =
        attackedOverGs
          .gameLog.events
          .ofType[GameEvent.CharacterBasicAttacked]

      basicAttackEvents.size should be(1)
      basicAttackEvents.head.targetCharacterId should be(s.defaultCharacter.id)

      val damagedEvents =
        attackedOverGs
          .gameLog.events
          .ofType[GameEvent.CharacterDamaged]

      damagedEvents.size should be(1)
      damagedEvents.head.characterId should be(s.defaultCharacter.id)
    }
    "not reveal parent when enemy ranged character attacks over them" in {
      val attackedOverGs = enemyTurnGs
        .setAttackType(s.defaultEnemy.id, AttackType.Ranged)
        .basicAttack(s.defaultEnemy.id, s.p(0)(1).character.id)

      assertEffectExistsOfType[effects.Invisibility](s.defaultCharacter.id)(attackedOverGs)
    }
    "reveal parent on parent basic attack" in {
      val basicAttackGs = eGs.basicAttack(s.defaultCharacter.id, s.defaultEnemy.id)
      assertEffectDoesNotExistsOfType[effects.Invisibility](s.defaultCharacter.id)(basicAttackGs)
    }
    "reveal parent on parent contact ability use" in {
      val aGs = eGs.useAbilityOnCharacter(defaultCharacterContactAbilityId, s.defaultEnemy.id)
      assertEffectDoesNotExistsOfType[effects.Invisibility](s.defaultCharacter.id)(aGs)
    }
    "not reveal parent on parent non-contact ability use" in {
      val aGs = eGs.useAbilityOnCoordinates(defaultCharacterAsterId, s.p(1)(0).spawnCoordinates)
      assertEffectExistsOfType[effects.Invisibility](s.defaultCharacter.id)(aGs)
    }
  }
}
