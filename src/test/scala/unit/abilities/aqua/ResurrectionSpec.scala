package unit.abilities.aqua

import com.tosware.nkm.models.{Damage, DamageType, GameStateValidator}
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.aqua.Resurrection
import com.tosware.nkm.models.game.hex.HexCoordinates
import com.tosware.nkm.providers.HexMapProvider.TestHexMapName
import helpers.TestUtils
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class ResurrectionSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(Resurrection.metadata.id))
  implicit val gameState = getTestGameState(TestHexMapName.Simple2v2, Seq(
    Seq(metadata.copy(name = "Empty1"), metadata.copy(name = "Empty2")),
    Seq(metadata.copy(name = "Empty3"), metadata.copy(name = "Empty4")),
  ))

  val p0FirstCharacterSpawnCoordinates = HexCoordinates(0, 0)
  val p0SecondCharacterSpawnCoordinates = HexCoordinates(-1, 0)
  val p1FirstCharacterSpawnCoordinates = HexCoordinates(3, 0)
  val p1SecondCharacterSpawnCoordinates = HexCoordinates(4, 0)

  val p0FirstCharacter = characterOnPoint(p0FirstCharacterSpawnCoordinates)
  val p0SecondCharacter = characterOnPoint(p0SecondCharacterSpawnCoordinates)

  val p1FirstCharacter = characterOnPoint(p1FirstCharacterSpawnCoordinates)
  val p1SecondCharacter = characterOnPoint(p1SecondCharacterSpawnCoordinates)

  val abilityId = p0FirstCharacter.state.abilities.head.id

  Resurrection.metadata.name must {
    "be able to resurrect characters that died in the same phase" in {
      val deadGameState = gameState.damageCharacter(p0SecondCharacter.id, Damage(DamageType.True, 99999))(random, gameState.id)

      val r = GameStateValidator()(deadGameState)
        .validateAbilityUseOnCoordinates(p0FirstCharacter.owner.id, abilityId, p0SecondCharacterSpawnCoordinates, UseData(p0SecondCharacter.id))
      assertCommandSuccess(r)

      val resurrectedGameState: GameState = deadGameState.useAbilityOnCoordinates(abilityId, p0SecondCharacterSpawnCoordinates, UseData(p0SecondCharacter.id))
      val resurrectedCharacter = resurrectedGameState.characterById(p0SecondCharacter.id).get
      resurrectedCharacter.state.healthPoints should be (resurrectedCharacter.state.maxHealthPoints / 2)
      resurrectedCharacter.parentCell.map(_.coordinates) should be (Some(HexCoordinates(-1, 0)))
    }

    "be able to resurrect characters that died in phase before" in {
      val deadGameState = gameState.damageCharacter(p0SecondCharacter.id, Damage(DamageType.True, 99999))(random, gameState.id).incrementPhase()

      val r = GameStateValidator()(deadGameState)
        .validateAbilityUseOnCoordinates(p0FirstCharacter.owner.id, abilityId, p0SecondCharacterSpawnCoordinates, UseData(p0SecondCharacter.id))
      assertCommandSuccess(r)
    }

    "not be able to resurrect characters that died two phases ago" in {
      val deadGameState = gameState.damageCharacter(p0SecondCharacter.id, Damage(DamageType.True, 99999))(random, gameState.id).incrementPhase(2)

      val r = GameStateValidator()(deadGameState)
        .validateAbilityUseOnCoordinates(p0FirstCharacter.owner.id, abilityId, p0SecondCharacterSpawnCoordinates, UseData(p0SecondCharacter.id))
      assertCommandFailure(r)
    }

    "not be able to resurrect foreign characters" in {
      val deadGameState = gameState
        .damageCharacter(p1SecondCharacter.id, Damage(DamageType.True, 99999))(random, gameState.id)
        .removeCharacterFromMap(p0SecondCharacter.id)(random, gameState.id)

      val r = GameStateValidator()(deadGameState)
        .validateAbilityUseOnCoordinates(p0FirstCharacter.owner.id, abilityId, p1SecondCharacterSpawnCoordinates, UseData(p1SecondCharacter.id))
      assertCommandFailure(r)

      val r2 = GameStateValidator()(deadGameState)
        .validateAbilityUseOnCoordinates(p0FirstCharacter.owner.id, abilityId, p0SecondCharacterSpawnCoordinates, UseData(p1SecondCharacter.id))
      assertCommandFailure(r2)
    }

    "not be able to resurrect on foreign spawn" in {
      val deadGameState = gameState
        .damageCharacter(p0SecondCharacter.id, Damage(DamageType.True, 99999))(random, gameState.id)
        .removeCharacterFromMap(p1FirstCharacter.id)(random, gameState.id)

      val r = GameStateValidator()(deadGameState)
        .validateAbilityUseOnCoordinates(p0FirstCharacter.owner.id, abilityId, p1FirstCharacterSpawnCoordinates, UseData(p0SecondCharacter.id))
      assertCommandFailure(r)
    }
    "not be able to resurrect on tiles that are not free to stand" in {
      val deadGameState = gameState.damageCharacter(p0SecondCharacter.id, Damage(DamageType.True, 99999))(random, gameState.id)

      val r = GameStateValidator()(deadGameState)
        .validateAbilityUseOnCoordinates(p0FirstCharacter.owner.id, abilityId, p0FirstCharacterSpawnCoordinates, UseData(p0SecondCharacter.id))
      assertCommandFailure(r)
    }

    "not be able to resurrect characters that are alive" in {
      val aliveGameState = gameState.removeCharacterFromMap(p0SecondCharacter.id)(random, gameState.id)

      val r = GameStateValidator()(aliveGameState)
        .validateAbilityUseOnCoordinates(p0FirstCharacter.owner.id, abilityId, p0SecondCharacterSpawnCoordinates, UseData(p0SecondCharacter.id))
      assertCommandFailure(r)
    }
  }

}