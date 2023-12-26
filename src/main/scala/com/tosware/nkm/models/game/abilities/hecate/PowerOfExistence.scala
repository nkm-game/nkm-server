package com.tosware.nkm.models.game.abilities.hecate

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.HexCoordinates

import scala.util.Random

object PowerOfExistence extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Power Of Existence",
      alternateName = "存在の力 (Sonzai no Chikara)",
      abilityType = AbilityType.Ultimate,
      description =
        """Release collected Life Energy, dealing magic damage to every enemy character on the map.
          |Damage equals to collected Life Energy divided by number of enemies.""".stripMargin,
    )
}

case class PowerOfExistence(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId) with Usable {
  override val metadata: AbilityMetadata = PowerOfExistence.metadata
  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    gameState.hexMap.cells.toCoords
  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords.whereSeenEnemiesOfC(parentCharacterId)
  override def use(useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val targets = targetsInRange.characters.map(_.id)
    val masterThroneOpt = parentCharacter.state.abilities.collectFirst { case a: MasterThrone => a }
    if (masterThroneOpt.isEmpty) return gameState
    val masterThrone = masterThroneOpt.get
    val damage = Damage(DamageType.Magical, masterThrone.collectedEnergy / targets.size)
    targets.foldLeft(gameState) { (acc, cid) =>
      hitAndDamageCharacter(cid, damage)(random, acc)
    }
  }
}
