package com.tosware.NKM.models.game.abilities.hecate

import com.tosware.NKM.NKMConf
import com.tosware.NKM.models.game.Ability.AbilityId
import com.tosware.NKM.models.game.NKMCharacter.CharacterId
import com.tosware.NKM.models.game._
import com.tosware.NKM.models.game.hex.HexCoordinates
import com.tosware.NKM.models.game.hex.HexUtils._
import com.tosware.NKM.models.{Damage, DamageType}

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
      cooldown = NKMConf.int("abilities.hecate.powerOfExistence.cooldown"),
    )
}

case class PowerOfExistence(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId) with UsableWithoutTarget {
  override val metadata = PowerOfExistence.metadata
  override val state: AbilityState = AbilityState(parentCharacterId)
  override def rangeCellCoords(implicit gameState: GameState) =
    gameState.hexMap.get.cells.toCoords
  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords.whereEnemiesOf(parentCharacterId)

  override def use()(implicit gameState: GameState): GameState = {
    val targets = targetsInRange.characters.map(_.id)
    val masterThroneOpt = parentCharacter.state.abilities.collectFirst { case a: MasterThrone => a }
    if(masterThroneOpt.isEmpty) return gameState
    val masterThrone = masterThroneOpt.get
    val damage = Damage(DamageType.Magical, masterThrone.collectedEnergy / targets.size)
    targets.foldLeft(gameState)((acc, cid) => {
      hitCharacter(cid, damage)(acc)
    }).updateAbility(masterThrone.id, masterThrone.reset())
  }

  private def hitCharacter(target: CharacterId, damage: Damage)(implicit gameState: GameState): GameState =
    gameState
      .abilityHitCharacter(id, target)
      .damageCharacter(target, damage)(id)
}
