package com.tosware.nkm.models.game.abilities.hecate

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.Ability.AbilityId
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.hex.HexCoordinates
import com.tosware.nkm.models.game.hex.HexUtils._
import com.tosware.nkm.models.{Damage, DamageType}

import scala.util.Random

object PowerOfExistence {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Power Of Existence",
      alternateName = "存在の力",
      abilityType = AbilityType.Ultimate,
      description =
        """Character releases collected Life Energy, dealing magic damage to every enemy character on the map.
          |Damage equals to collected Life Energy divided by number of enemies.
          |""".stripMargin,
      variables = NkmConf.extract("abilities.hecate.powerOfExistence"),
    )
}

case class PowerOfExistence(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId) with UsableWithoutTarget {
  override val metadata = PowerOfExistence.metadata
  override val state: AbilityState = AbilityState(parentCharacterId)
  override def rangeCellCoords(implicit gameState: GameState) =
    gameState.hexMap.get.cells.toCoords
  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords.whereEnemiesOfC(parentCharacterId)

  override def use()(implicit random: Random, gameState: GameState): GameState = {
    val targets = targetsInRange.characters.map(_.id)
    val masterThroneOpt = parentCharacter.state.abilities.collectFirst { case a: MasterThrone => a }
    if(masterThroneOpt.isEmpty) return gameState
    val masterThrone = masterThroneOpt.get
    val damage = Damage(DamageType.Magical, masterThrone.collectedEnergy / targets.size)
    targets.foldLeft(gameState)((acc, cid) => {
      hitCharacter(cid, damage)(random, acc)
    }).updateAbility(masterThrone.id, masterThrone.reset())
  }

  private def hitCharacter(target: CharacterId, damage: Damage)(implicit random: Random, gameState: GameState): GameState =
    gameState
      .abilityHitCharacter(id, target)
      .damageCharacter(target, damage)(random, id)
}
