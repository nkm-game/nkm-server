package unit.validators

import com.tosware.nkm.*
import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.hecate.PowerOfExistence
import com.tosware.nkm.models.game.abilities.sinon.TacticalEscape
import com.tosware.nkm.models.game.character.{AttackType, CharacterMetadata}
import com.tosware.nkm.models.game.effects.*
import com.tosware.nkm.models.game.hex.{HexCoordinates, TestHexMapName}
import helpers.{TestScenario, TestUtils}

class GameStateValidatorSpec extends TestUtils {
  private val metadata = CharacterMetadata.empty()
    .copy(
      initialSpeed = 3,
      initialBasicAttackRange = 1,
      initialAbilitiesMetadataIds = Seq(PowerOfExistence.metadata.id, TacticalEscape.metadata.id),
    )

  private val s = TestScenario.generate(TestHexMapName.Simple2v2, metadata)
  implicit private val gameState: GameState = s.gameState

  private val wallMeleeScenario = TestScenario.generate(
    TestHexMapName.Simple2v2Wall,
    metadata.copy(attackType = AttackType.Melee, initialBasicAttackRange = 4),
  )

  private val wallRangedScenario = TestScenario.generate(
    TestHexMapName.Simple2v2Wall,
    metadata.copy(attackType = AttackType.Ranged, initialBasicAttackRange = 4),
  )

  private val ultimateAbilityId = s.p(0)(1).character.state.abilities.head.id
  private val normalAbilityId = s.p(0)(1).character.state.abilities(1).id
  private val validator = GameStateValidator()(gameState)

  "GameStateValidator" must {
    "pass sanity check" in {
      gameState.characters.count(_.isOnMap(gameState)) should be(s.gameState.characters.size)
      gameState.hexMap.cells.whereCharacters.size should be(s.gameState.characters.size)
    }
    "validate moving characters and" when {
      "allow move within speed range" in {
        assertCommandSuccess {
          validator.validateBasicMoveCharacter(
            gameState.players(0).id,
            CoordinateSeq((0, 0), (1, 0)),
            s.p(0)(1).character.id,
          )
        }
      }

      "allow move over friendly characters" in {
        assertCommandSuccess {
          validator.validateBasicMoveCharacter(
            gameState.players(0).id,
            CoordinateSeq((0, 0), (-1, 0), (-2, 0)),
            s.p(0)(1).character.id,
          )
        }

        val newGameState = gameState.basicMoveCharacter(s.p(0)(1).character.id, CoordinateSeq((0, 0), (-1, 0), (-2, 0)))
        newGameState.characters.count(_.isOnMap(newGameState)) should be(s.gameState.characters.size)
        newGameState.hexMap.cells.whereCharacters.size should be(s.gameState.characters.size)
      }

      "disallow if character is not on the map" in {
        val newGameState = gameState.removeCharacterFromMap(s.p(0)(1).character.id)

        assertCommandFailure {
          GameStateValidator()(newGameState).validateBasicMoveCharacter(
            gameState.players(0).id,
            CoordinateSeq((0, 0), (1, 0)),
            s.p(0)(1).character.id,
          )
        }
      }

      "disallow if character is grounded" in {
        val newGameState = gameState.addEffect(s.p(0)(1).character.id, Ground(randomUUID(), 1))

        assertCommandFailure {
          GameStateValidator()(newGameState).validateBasicMoveCharacter(
            gameState.players(0).id,
            CoordinateSeq((0, 0), (1, 0)),
            s.p(0)(1).character.id,
          )
        }
      }

      "disallow if character is snared" in {
        val newGameState = gameState.addEffect(s.p(0)(1).character.id, Snare(randomUUID(), 1))

        assertCommandFailure {
          GameStateValidator()(newGameState).validateBasicMoveCharacter(
            gameState.players(0).id,
            CoordinateSeq((0, 0), (1, 0)),
            s.p(0)(1).character.id,
          )
        }
      }

      "disallow if character is stunned" in {
        val newGameState = gameState.addEffect(s.p(0)(1).character.id, Stun(randomUUID(), 1))

        assertCommandFailure {
          GameStateValidator()(newGameState).validateBasicMoveCharacter(
            gameState.players(0).id,
            CoordinateSeq((0, 0), (1, 0)),
            s.p(0)(1).character.id,
          )
        }
      }

      "disallow empty moves" in {
        assertCommandFailure {
          validator.validateBasicMoveCharacter(gameState.players(0).id, CoordinateSeq(), s.p(0)(1).character.id)
        }
        assertCommandFailure {
          validator.validateBasicMoveCharacter(gameState.players(0).id, CoordinateSeq((0, 0)), s.p(0)(1).character.id)
        }
      }

      "disallow move from other cell than characters" in {
        assertCommandFailure {
          validator.validateBasicMoveCharacter(
            gameState.players(0).id,
            CoordinateSeq((1, 0), (2, 0)),
            s.p(0)(1).character.id,
          )
        }
      }

      "disallow move outside of turn" in {
        assertCommandFailure {
          validator.validateBasicMoveCharacter(
            gameState.players(1).id,
            CoordinateSeq((3, 0), (2, 0)),
            s.defaultEnemy.id,
          )
        }
      }

      "disallow move foreign characters" in {
        assertCommandFailure {
          validator.validateBasicMoveCharacter(
            gameState.players(0).id,
            CoordinateSeq((3, 0), (2, 0)),
            s.defaultEnemy.id,
          )
        }
      }

      "disallow move above speed range" in {
        assertCommandFailure {
          validator.validateBasicMoveCharacter(
            gameState.players(0).id,
            CoordinateSeq((0, 0), (1, 0), (2, 0), (2, 1), (1, 1)),
            s.p(0)(1).character.id,
          )
        }
      }

      "disallow move into the same position" in {
        assertCommandFailure {
          validator.validateBasicMoveCharacter(
            gameState.players(0).id,
            CoordinateSeq((0, 0), (1, 0), (0, 0)),
            s.p(0)(1).character.id,
          )
        }
      }

      "disallow move that visits another cell more than once" in {
        assertCommandFailure {
          validator.validateBasicMoveCharacter(
            gameState.players(0).id,
            CoordinateSeq((0, 0), (1, 0), (2, 0), (1, 0)),
            s.p(0)(1).character.id,
          )
        }
      }

      "disallow move if character already moved" in {
        val newGameState = gameState.basicMoveCharacter(
          s.p(0)(1).character.id,
          CoordinateSeq((0, 0), (1, 0)),
        )

        assertCommandFailure {
          GameStateValidator()(newGameState).validateBasicMoveCharacter(
            gameState.players(0).id,
            CoordinateSeq((1, 0), (0, 0)),
            s.p(0)(1).character.id,
          )
        }
      }

      "disallow move if character used ultimate ability" in {
        val newGameState = gameState.useAbility(ultimateAbilityId)

        assertCommandFailure {
          GameStateValidator()(newGameState).validateBasicMoveCharacter(
            gameState.players(0).id,
            CoordinateSeq((0, 0), (1, 0)),
            s.p(0)(1).character.id,
          )
        }
      }

      "disallow move if other character took action in turn" in {
        val newGameState = gameState.takeActionWithCharacter("test_nonexistent_id")

        assertCommandFailure {
          GameStateValidator()(newGameState).validateBasicMoveCharacter(
            gameState.players(0).id,
            CoordinateSeq((0, 0), (1, 0)),
            s.p(0)(1).character.id,
          )

        }
      }

      "disallow move if there is an obstacle on path" in {
        assertCommandFailure {
          validator.validateBasicMoveCharacter(
            gameState.players(0).id,
            CoordinateSeq((0, 0), (1, -1), (1, 0)),
            s.p(0)(1).character.id,
          )
        }
      }

      "disallow move if cell at the end is not free to move" in {
        assertCommandFailure {
          validator.validateBasicMoveCharacter(
            gameState.players(0).id,
            CoordinateSeq((0, 0), (1, -1)),
            s.p(0)(1).character.id,
          )
        }
      }

      "disallow move if moving to not adjacent cell" in {
        assertCommandFailure {
          validator.validateBasicMoveCharacter(
            gameState.players(0).id,
            CoordinateSeq((0, 0), (0, 2)),
            s.p(0)(1).character.id,
          )
        }
      }
    }

    "validate attacking characters and" when {
      val moveGameState = gameState.teleportCharacter(s.p(0)(1).character.id, HexCoordinates(2, 0))

      "allow if character is in attack range without obstacles" in {
        assertCommandSuccess {
          GameStateValidator()(moveGameState).validateBasicAttackCharacter(
            s.p(0)(1).character.owner.id,
            s.p(0)(1).character.id,
            s.defaultEnemy.id,
          )
        }
      }

      "allow over wall if character is ranged" in {
        val s = wallRangedScenario
        implicit val gameState: GameState = s.gameState
        assertCommandSuccess {
          GameStateValidator().validateBasicAttackCharacter(
            s.p(0)(1).character.owner.id,
            s.p(0)(1).character.id,
            s.defaultEnemy.id,
          )
        }
      }

      "disallow over wall if character is melee" in {
        val s = wallMeleeScenario
        implicit val gameState: GameState = s.gameState
        assertCommandFailure {
          GameStateValidator().validateBasicAttackCharacter(
            s.p(0)(1).character.owner.id,
            s.p(0)(1).character.id,
            s.defaultEnemy.id,
          )
        }
      }

      "allow over character if character is ranged" in {
        val s = wallRangedScenario
        implicit val gameState: GameState = s.gameState
        assertCommandSuccess {
          GameStateValidator().validateBasicAttackCharacter(
            s.p(0)(1).character.owner.id,
            s.p(0)(1).character.id,
            s.p(1)(1).character.id,
          )
        }
      }

      "disallow over character if character is melee" in {
        val s = wallMeleeScenario
        implicit val gameState: GameState = s.gameState
        assertCommandFailure {
          GameStateValidator().validateBasicAttackCharacter(
            s.p(0)(1).character.owner.id,
            s.p(0)(1).character.id,
            s.p(1)(1).character.id,
          )
        }
      }

      "disallow if character is not on the map" in {
        val newGameState = moveGameState.removeCharacterFromMap(s.p(0)(1).character.id)

        assertCommandFailure {
          GameStateValidator()(newGameState).validateBasicAttackCharacter(
            gameState.players(0).id,
            s.p(0)(1).character.id,
            s.defaultEnemy.id,
          )
        }
      }

      "allow if character is grounded" in {
        val newGameState = moveGameState.addEffect(s.p(0)(1).character.id, Ground(randomUUID(), 1))

        assertCommandSuccess {
          GameStateValidator()(newGameState).validateBasicAttackCharacter(
            gameState.players(0).id,
            s.p(0)(1).character.id,
            s.defaultEnemy.id,
          )
        }
      }

      "allow if character is snared" in {
        val newGameState = moveGameState.addEffect(s.p(0)(1).character.id, Snare(randomUUID(), 1))

        assertCommandSuccess {
          GameStateValidator()(newGameState).validateBasicAttackCharacter(
            gameState.players(0).id,
            s.p(0)(1).character.id,
            s.defaultEnemy.id,
          )
        }
      }

      "disallow character is stunned" in {
        val newGameState = moveGameState.addEffect(s.p(0)(1).character.id, Stun(randomUUID(), 1))

        assertCommandFailure {
          GameStateValidator()(newGameState).validateBasicAttackCharacter(
            gameState.players(0).id,
            s.p(0)(1).character.id,
            s.defaultEnemy.id,
          )
        }
      }

      "disallow character is disarmed" in {
        val newGameState = moveGameState.addEffect(s.p(0)(1).character.id, Disarm(randomUUID(), 1))

        assertCommandFailure {
          GameStateValidator()(newGameState).validateBasicAttackCharacter(
            gameState.players(0).id,
            s.p(0)(1).character.id,
            s.defaultEnemy.id,
          )
        }
      }

      "disallow if character is not in attack range" in {
        assertCommandFailure {
          validator.validateBasicAttackCharacter(
            gameState.players(0).id,
            s.p(0)(1).character.id,
            s.defaultEnemy.id,
          )
        }
      }

      "disallow if character already basic attacked" in {
        val newState = moveGameState.basicAttack(s.p(0)(1).character.id, s.defaultEnemy.id)

        assertCommandFailure {
          GameStateValidator()(newState).validateBasicAttackCharacter(
            gameState.players(0).id,
            s.p(0)(1).character.id,
            s.defaultEnemy.id,
          )
        }
      }

      "disallow if character used ultimate ability" in {
        val newGameState = moveGameState.useAbility(ultimateAbilityId)

        assertCommandFailure {
          GameStateValidator()(newGameState).validateBasicAttackCharacter(
            gameState.players(0).id,
            s.p(0)(1).character.id,
            s.defaultEnemy.id,
          )
        }
      }

      "disallow move if other character took action in turn" in {
        val newGameState = moveGameState.takeActionWithCharacter("test_nonexistent_id")

        assertCommandFailure {
          GameStateValidator()(newGameState).validateBasicAttackCharacter(
            gameState.players(0).id,
            s.p(0)(1).character.id,
            s.defaultEnemy.id,
          )
        }
      }
    }

    "validate using abilities" when {
      "allow use of ability" in {
        val incrementGameState = gameState.incrementPhase(3)
        assertCommandSuccess {
          GameStateValidator()(incrementGameState)
            .validateAbilityUse(s.p(0)(1).character.owner.id, ultimateAbilityId)
        }
      }

      "disallow use of ultimate ability before phase 4" in {
        assertCommandFailure {
          GameStateValidator()(gameState)
            .validateAbilityUse(s.p(0)(1).character.owner.id, ultimateAbilityId)
        }

        val increment2GameState = gameState.incrementPhase(2)
        assertCommandFailure {
          GameStateValidator()(increment2GameState)
            .validateAbilityUse(s.p(0)(1).character.owner.id, ultimateAbilityId)
        }

        val increment3GameState = gameState.incrementPhase(3)
        assertCommandSuccess {
          GameStateValidator()(increment3GameState)
            .validateAbilityUse(s.p(0)(1).character.owner.id, ultimateAbilityId)
        }
      }

      "disallow use of ability on cooldown" in {
        val incrementGameState = gameState.incrementPhase(3)
        val newGameState = incrementGameState.useAbility(ultimateAbilityId)
          .endTurn()
          .passTurn(s.defaultEnemy.id)
        newGameState.abilityById(ultimateAbilityId).state(newGameState).cooldown should be > 0

        assertCommandFailure {
          GameStateValidator()(newGameState)
            .validateAbilityUse(s.p(0)(1).character.owner.id, ultimateAbilityId)
        }
      }
      "disallow using ability another time in phase" in {
        val incrementGameState = gameState.incrementPhase(3)
        val newGameState = incrementGameState.useAbility(ultimateAbilityId)
          .endTurn()
          .passTurn(s.defaultEnemy.id)
          .decrementAbilityCooldown(ultimateAbilityId, 999)
        newGameState.abilityById(ultimateAbilityId).state(newGameState).cooldown should be(0)

        assertCommandFailure {
          GameStateValidator()(newGameState)
            .validateAbilityUse(s.p(0)(1).character.owner.id, ultimateAbilityId)
        }
      }

      "disallow using ability while stunned" in {
        val stunEffect = effects.Stun(randomUUID(), 1)
        val stunnedGameState = gameState
          .incrementPhase(3)
          .addEffect(s.p(0)(1).character.id, stunEffect)

        assertCommandFailure {
          GameStateValidator()(stunnedGameState)
            .validateAbilityUse(s.p(0)(1).character.owner.id, ultimateAbilityId)
        }
      }
    }
    "disallow moving for a second time in one phase" in {
      val newGameState = gameState.basicMoveCharacter(
        s.p(0)(1).character.id,
        CoordinateSeq((0, 0), (1, 0)),
      ).endTurn()
        .passTurn(s.defaultEnemy.id)

      assertCommandFailure {
        GameStateValidator()(newGameState).validateBasicMoveCharacter(
          gameState.players(0).id,
          CoordinateSeq((1, 0), (0, 0)),
          s.p(0)(1).character.id,
        )
      }
    }

    "disallow basic attacking for a second time in one phase" in {
      val moveGameState = gameState.teleportCharacter(s.p(0)(1).character.id, HexCoordinates(2, 0))
      val newGameState = moveGameState
        .basicAttack(s.p(0)(1).character.id, s.defaultEnemy.id)
        .endTurn()
        .passTurn(s.defaultEnemy.id)

      assertCommandFailure {
        GameStateValidator()(newGameState).validateBasicAttackCharacter(
          gameState.players(0).id,
          s.p(0)(1).character.id,
          s.defaultEnemy.id,
        )
      }
    }
    "disallow basic attacking after using a basic ability" in {
      val incrementGameState = gameState.incrementPhase(3)
      val moveGameState = incrementGameState.teleportCharacter(s.p(0)(1).character.id, HexCoordinates(2, 0))

      val newGameState = moveGameState.useAbility(normalAbilityId)

      assertCommandFailure {
        GameStateValidator()(newGameState).validateBasicAttackCharacter(
          gameState.players(0).id,
          s.p(0)(1).character.id,
          s.defaultEnemy.id,
        )
      }
    }
  }
}
