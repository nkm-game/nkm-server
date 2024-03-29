package com.tosware.nkm.models.game.abilities.liones_elizabeth

import com.tosware.nkm.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.HexCoordinates

import scala.util.Random

object PowerOfTheGoddess extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Power Of The Goddess",
      abilityType = AbilityType.Ultimate,
      description = "Heal all allied characters on the map for {heal} HP.",
    )
}

case class PowerOfTheGoddess(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId) with Usable {
  override val metadata: AbilityMetadata = PowerOfTheGoddess.metadata
  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    gameState.hexMap.cells.toCoords
  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords.whereFriendsOfC(parentCharacterId)
  override def use(useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val targets = targetsInRange.characters.map(_.id)
    targets.foldLeft(gameState) { (acc, cid) =>
      acc.heal(cid, metadata.variables("heal"))(random, id)
    }
  }
}
