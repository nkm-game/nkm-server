package unit.abilities.hecate

import com.tosware.nkm.models.GameStateValidator
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.abilities.hecate.{MasterThrone, PowerOfExistence}
import com.tosware.nkm.models.game.hex.HexCoordinates
import com.tosware.nkm.models.game.hex.HexUtils._
import com.tosware.nkm.providers.HexMapProvider.TestHexMapName
import helpers.TestUtils
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class PowerOfExistenceSpec
  extends AnyWordSpecLike
    with Matchers
    with TestUtils
{
  val metadata = CharacterMetadata.empty().copy(initialAbilitiesMetadataIds = Seq(PowerOfExistence.metadata.id, MasterThrone.metadata.id))
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
  val masterThroneAbilityId = p0FirstCharacter.state.abilities.tail.head.id

  PowerOfExistence.metadata.name must {
    "be able to damage characters" in {
      val aaGameState: GameState = gameState.basicAttack(p0FirstCharacter.id, p1FirstCharacter.id)
      aaGameState.abilityById(masterThroneAbilityId).get.asInstanceOf[MasterThrone].collectedEnergy should be > 0

      val validator = GameStateValidator()(aaGameState)
      val r = validator.validateAbilityUseWithoutTarget(p0FirstCharacter.owner.id, abilityId)
      assertCommandSuccess(r)

      val abilityUsedGameState: GameState = aaGameState.useAbilityWithoutTarget(abilityId)
      abilityUsedGameState.abilityById(masterThroneAbilityId).get.asInstanceOf[MasterThrone].collectedEnergy should be(0)

      abilityUsedGameState.gameLog.events
        .ofType[GameEvent.CharacterDamaged]
        .causedBy(abilityId)
        .size should be (2)
    }
  }
}