package unit.validators

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.hecate.PowerOfExistence
import com.tosware.nkm.models.game.effects._
import com.tosware.nkm.models.game.hex.{HexCoordinates, NkmUtils}
import com.tosware.nkm.models.game.hex.HexUtils.CoordinateSeq
import com.tosware.nkm.providers.HexMapProvider.TestHexMapName
import helpers.TestUtils
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class GameStateValidatorSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  val metadata = CharacterMetadata.empty()
    .copy(
      initialSpeed = 3,
      initialBasicAttackRange = 1,
      initialAbilitiesMetadataIds = Seq(PowerOfExistence.metadata.id)
    )
  implicit val gameState = getTestGameState(TestHexMapName.Simple2v2, Seq(
    Seq(metadata.copy(name = "Empty1"), metadata.copy(name = "Empty2")),
    Seq(metadata.copy(name = "Empty3"), metadata.copy(name = "Empty4")),
  ))

  val wallMeleeGameState = getTestGameState(TestHexMapName.Simple2v2Wall, Seq(
    Seq(
      metadata.copy(name = "Empty1", attackType = AttackType.Melee, initialBasicAttackRange = 4),
      metadata.copy(name = "Empty2", attackType = AttackType.Melee, initialBasicAttackRange = 4),
    ),
    Seq(
      metadata.copy(name = "Empty2", attackType = AttackType.Melee, initialBasicAttackRange = 4),
      metadata.copy(name = "Empty3", attackType = AttackType.Melee, initialBasicAttackRange = 4),
    ),
  ))

  val wallRangedGameState = getTestGameState(TestHexMapName.Simple2v2Wall, Seq(
    Seq(
      metadata.copy(name = "Empty1", attackType = AttackType.Ranged, initialBasicAttackRange = 4),
      metadata.copy(name = "Empty2", attackType = AttackType.Ranged, initialBasicAttackRange = 4),
    ),
    Seq(
      metadata.copy(name = "Empty3", attackType = AttackType.Ranged, initialBasicAttackRange = 4),
      metadata.copy(name = "Empty4", attackType = AttackType.Ranged, initialBasicAttackRange = 4),
    ),
  ))

  def characterIdOnPoint(hexCoordinates: HexCoordinates) = gameState.hexMap.get.getCell(hexCoordinates).get.characterId.get

  val p0FirstCharacterSpawnCoordinates = HexCoordinates(0, 0)
  val p0SecondCharacterSpawnCoordinates = HexCoordinates(-1, 0)
  val p1FirstCharacterSpawnCoordinates = HexCoordinates(3, 0)
  val p1SecondCharacterSpawnCoordinates = HexCoordinates(4, 0)

  val p0FirstCharacter = characterOnPoint(p0FirstCharacterSpawnCoordinates)
  val p0SecondCharacter = characterOnPoint(p0SecondCharacterSpawnCoordinates)

  val p1FirstCharacter = characterOnPoint(p1FirstCharacterSpawnCoordinates)
  val p1SecondCharacter = characterOnPoint(p1SecondCharacterSpawnCoordinates)

  val p0FirstCharacterMelee = characterOnPoint(p0FirstCharacterSpawnCoordinates)(wallMeleeGameState)
  val p1FirstCharacterMelee = characterOnPoint(p1FirstCharacterSpawnCoordinates)(wallMeleeGameState)
  val p1SecondCharacterMelee = characterOnPoint(p1SecondCharacterSpawnCoordinates)(wallMeleeGameState)

  val p0FirstCharacterRanged = characterOnPoint(p0FirstCharacterSpawnCoordinates)(wallRangedGameState)
  val p1FirstCharacterRanged = characterOnPoint(p1FirstCharacterSpawnCoordinates)(wallRangedGameState)
  val p1SecondCharacterRanged = characterOnPoint(p1SecondCharacterSpawnCoordinates)(wallRangedGameState)

  val abilityId = p0FirstCharacter.state.abilities.head.id

  private val validator = GameStateValidator()(gameState)

  "GameStateValidator" must {
    "validate moving characters and" when {
      "allow move within speed range" in {
        val result = validator.validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((0, 0), (1, 0)),
          p0FirstCharacter.id
        )
        assertCommandSuccess(result)
      }

      "allow move over friendly characters" in {
        val result = validator.validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((0, 0), (-1, 0), (-2, 0)),
          p0FirstCharacter.id
        )
        assertCommandSuccess(result)
      }

      "disallow if character is not on the map" in {
        val newGameState = gameState.removeCharacterFromMap(p0FirstCharacter.id)(random, gameState.id)
        val result =  GameStateValidator()(newGameState).validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((0, 0), (1, 0)),
          p0FirstCharacter.id
        )
        assertCommandFailure(result)
      }

      "disallow if character is grounded" in {
        val newGameState = gameState.addEffect(p0FirstCharacter.id, GroundEffect(NkmUtils.randomUUID(), 1))(random, gameState.id)

        val result = GameStateValidator()(newGameState).validateBasicMoveCharacter(
          gameState.players(0).id,
          CoordinateSeq((0, 0), (1, 0)),
          p0FirstCharacter.id
        )
        assertCommandFailure(result)
      }

      "disallow if character is snared" in {
        val newGameState = gameState.addEffect(p0FirstCharacter.id, SnareEffect(NkmUtils.randomUUID(), 1))(random, gameState.id)

        val result = GameStateValidator()(newGameState).validateBasicMoveCharacter(
          gameState.players(0).id,
          CoordinateSeq((0, 0), (1, 0)),
          p0FirstCharacter.id
        )
        assertCommandFailure(result)
      }

      "disallow if character is stunned" in {
        val newGameState = gameState.addEffect(p0FirstCharacter.id, StunEffect(NkmUtils.randomUUID(), 1))(random, gameState.id)

        val result = GameStateValidator()(newGameState).validateBasicMoveCharacter(
          gameState.players(0).id,
          CoordinateSeq((0, 0), (1, 0)),
          p0FirstCharacter.id
        )
        assertCommandFailure(result)
      }

      "disallow empty moves" in {
        val result = validator.validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq(),
          p0FirstCharacter.id
        )
        assertCommandFailure(result)

        val result2 = validator.validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((0, 0)),
          p0FirstCharacter.id
        )
        assertCommandFailure(result2)
      }

      "disallow move from other cell than characters" in {
        val result = validator.validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((1, 0), (2, 0)),
          p0FirstCharacter.id
        )
        assertCommandFailure(result)
      }

      "disallow move outside of turn" in {
        val result = validator.validateBasicMoveCharacter(gameState.players(1).id,
          CoordinateSeq((3, 0), (2, 0)),
          p1FirstCharacter.id
        )
        assertCommandFailure(result)
      }

      "disallow move foreign characters" in {
        val result = validator.validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((3, 0), (2, 0)),
          p1FirstCharacter.id
        )
        assertCommandFailure(result)
      }


      "disallow move above speed range" in {
        val result = validator.validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((0, 0), (1, 0), (2, 0), (2, 1), (2, 2)),
          p0FirstCharacter.id
        )
        assertCommandFailure(result)
      }

      "disallow move into the same position" in {
        val result = validator.validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((0, 0), (1, 0), (0, 0)),
          p0FirstCharacter.id
        )
        assertCommandFailure(result)
      }

      "disallow move that visits another cell more than once" in {
        val result = validator.validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((0, 0), (1, 0), (2, 0), (1, 0)),
          p0FirstCharacter.id
        )
        assertCommandFailure(result)
      }

      "disallow move if character already moved" in {
        val newGameState = gameState.basicMoveCharacter(
          p0FirstCharacter.id,
          CoordinateSeq((0, 0), (1, 0)))
        val result = GameStateValidator()(newGameState).validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((1, 0), (0, 0)),
          p0FirstCharacter.id
        )
        assertCommandFailure(result)
      }

      "disallow move if character used ultimate ability" in {
        val newGameState = gameState.useAbilityWithoutTarget(abilityId)
        val result = GameStateValidator()(newGameState).validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((0, 0), (1, 0)),
          p0FirstCharacter.id
        )
        assertCommandFailure(result)
      }

      "disallow move if other character took action in turn" in {
        val newGameState = gameState.takeActionWithCharacter("test_nonexistent_id")

        val result = GameStateValidator()(newGameState).validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((0, 0), (1, 0)),
          p0FirstCharacter.id
        )
        assertCommandFailure(result)
      }

      "disallow move if there is an obstacle on path" in {
        val result = validator.validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((0, 0), (1, -1), (1, 0)),
          p0FirstCharacter.id
        )
        assertCommandFailure(result)
      }

      "disallow move if cell at the end is not free to move" in {
        val result = validator.validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((0, 0), (1, -1)),
          p0FirstCharacter.id
        )
        assertCommandFailure(result)
      }

      "disallow move if moving to not adjacent cell" in {
        val result = validator.validateBasicMoveCharacter(gameState.players(0).id,
          CoordinateSeq((0, 0), (0, 2)),
          p0FirstCharacter.id
        )
        assertCommandFailure(result)
      }
    }

    "validate attacking characters and" when {
      val moveGameState = gameState.teleportCharacter(p0FirstCharacter.id, HexCoordinates(2, 0))(random, gameState.id)


      "allow if character is in attack range without obstacles" in {
        val result = GameStateValidator()(moveGameState).validateBasicAttackCharacter(p0FirstCharacter.owner.id,
          p0FirstCharacter.id,
          p1FirstCharacter.id,
        )

        assertCommandSuccess(result)
      }

      "allow over wall if character is ranged" in {
        implicit val gameState: GameState = wallRangedGameState
        val result = GameStateValidator().validateBasicAttackCharacter(p0FirstCharacterRanged.owner.id,
          p0FirstCharacterRanged.id,
          p1FirstCharacterRanged.id,
        )

        assertCommandSuccess(result)
      }

      "disallow over wall if character is melee" in {
        implicit val gameState: GameState = wallMeleeGameState
        val result = GameStateValidator().validateBasicAttackCharacter(p0FirstCharacterMelee.owner.id,
          p0FirstCharacterMelee.id,
          p1FirstCharacterMelee.id,
        )

        assertCommandFailure(result)
      }

      "allow over character if character is ranged" in {
        implicit val gameState: GameState = wallRangedGameState
        val result = GameStateValidator().validateBasicAttackCharacter(p0FirstCharacterRanged.owner.id,
          p0FirstCharacterRanged.id,
          p1SecondCharacterRanged.id,
        )

        assertCommandSuccess(result)
      }

      "disallow over character if character is melee" in {
        implicit val gameState: GameState = wallMeleeGameState
        val result = GameStateValidator().validateBasicAttackCharacter(p0FirstCharacterMelee.owner.id,
          p0FirstCharacterMelee.id,
          p1SecondCharacterMelee.id,
        )

        assertCommandFailure(result)
      }

      "disallow if character is not on the map" in {
        val newGameState = moveGameState.removeCharacterFromMap(p0FirstCharacter.id)(random, gameState.id)

        val result = GameStateValidator()(newGameState).validateBasicAttackCharacter(gameState.players(0).id,
          p0FirstCharacter.id,
          p1FirstCharacter.id,
        )
        assertCommandFailure(result)
      }

      "allow if character is grounded" in {
        val newGameState = moveGameState.addEffect(p0FirstCharacter.id, GroundEffect(NkmUtils.randomUUID(), 1))(random, gameState.id)

        val result = GameStateValidator()(newGameState).validateBasicAttackCharacter(
          gameState.players(0).id,
          p0FirstCharacter.id,
          p1FirstCharacter.id,
        )
        assertCommandSuccess(result)
      }

      "allow if character is snared" in {
        val newGameState = moveGameState.addEffect(p0FirstCharacter.id, SnareEffect(NkmUtils.randomUUID(), 1))(random, gameState.id)

        val result = GameStateValidator()(newGameState).validateBasicAttackCharacter(
          gameState.players(0).id,
          p0FirstCharacter.id,
          p1FirstCharacter.id,
        )
        assertCommandSuccess(result)
      }

      "disallow character is stunned" in {
        val newGameState = moveGameState.addEffect(p0FirstCharacter.id, StunEffect(NkmUtils.randomUUID(), 1))(random, gameState.id)

        val result = GameStateValidator()(newGameState).validateBasicAttackCharacter(
          gameState.players(0).id,
          p0FirstCharacter.id,
          p1FirstCharacter.id,
        )
        assertCommandFailure(result)
      }

      "disallow character is disarmed" in {
        val newGameState = moveGameState.addEffect(p0FirstCharacter.id, DisarmEffect(NkmUtils.randomUUID(), 1))(random, gameState.id)

        val result = GameStateValidator()(newGameState).validateBasicAttackCharacter(
          gameState.players(0).id,
          p0FirstCharacter.id,
          p1FirstCharacter.id,
        )
        assertCommandFailure(result)
      }

      "disallow if character is not in attack range" in {
        val result = validator.validateBasicAttackCharacter(gameState.players(0).id,
          p0FirstCharacter.id,
          p1FirstCharacter.id,
        )

        assertCommandFailure(result)
      }

      "disallow if character already basic attacked" in {
        val newState = moveGameState.basicAttack(p0FirstCharacter.id, p1FirstCharacter.id)
        val result = GameStateValidator()(newState).validateBasicAttackCharacter(gameState.players(0).id,
          p0FirstCharacter.id,
          p1FirstCharacter.id,
        )
        assertCommandFailure(result)
      }

      "disallow if character used ultimate ability" in {
        val newGameState = moveGameState.useAbilityWithoutTarget(abilityId)
        val result = GameStateValidator()(newGameState).validateBasicAttackCharacter(
          gameState.players(0).id,
          p0FirstCharacter.id,
          p1FirstCharacter.id,
        )
        assertCommandFailure(result)
      }

      "disallow move if other character took action in turn" in {
        val newGameState = moveGameState.takeActionWithCharacter("test_nonexistent_id")

        val result = GameStateValidator()(newGameState).validateBasicAttackCharacter(
          gameState.players(0).id,
          p0FirstCharacter.id,
          p1FirstCharacter.id,
        )
        assertCommandFailure(result)
      }
    }
  }
}
