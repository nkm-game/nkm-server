package com.tosware.NKM.models.game.abilities.aqua

import com.softwaremill.quicklens._
import com.tosware.NKM.models.game.NKMCharacter.CharacterId
import com.tosware.NKM.models.game._
import com.tosware.NKM.models.game.hex.HexCoordinates
import com.tosware.NKM.models.game.hex.HexUtils._

object Resurrection {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Resurrection",
      abilityType = AbilityType.Ultimate,
      description = "*Character* resurrects allied character, that died maximally one phase before.\nResurrected character respawns with half base HP on selected spawn point.",
      cooldown = 8,
    )
}

case class ResurrectionUseData(characterId: CharacterId)

case class Resurrection(parentCharacterId: CharacterId) extends Ability with UsableOnCoordinates {
  override def metadata = Resurrection.metadata
  override def state = AbilityState(parentCharacterId, metadata.cooldown)
  override def rangeCellCoords(implicit gameState: GameState) =
    gameState.hexMap.get.getSpawnPointsFor(parentCharacter.owner.id).map(_.coordinates)

  override def targetsInRange(implicit gameState: GameState) =
    rangeCellCoords.whereEmpty

  override def use(target: HexCoordinates, useData: UseData)(implicit gameState: GameState) = {
    val targetCharacterId = useData.data
    gameState.updateCharacter(targetCharacterId, c => {
      c.modify(_.state.healthPoints).setTo(c.state.maxHealthPoints / 2)
    }).placeCharacter(target, targetCharacterId)
  }
  // TODO: add validator
}
