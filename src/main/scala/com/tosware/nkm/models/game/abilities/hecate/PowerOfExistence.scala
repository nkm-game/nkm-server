package com.tosware.nkm.models.game.abilities.hecate

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.ability.Ability.AbilityId
import com.tosware.nkm.models.game.character.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.ability.{Ability, AbilityMetadata, AbilityType, Usable, UseData}
import com.tosware.nkm.models.game.hex.HexCoordinates

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

case class PowerOfExistence(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId) with Usable {
  override val metadata = PowerOfExistence.metadata
  override def rangeCellCoords(implicit gameState: GameState) =
    gameState.hexMap.cells.toCoords
  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords.whereEnemiesOfC(parentCharacterId)

  override def use(useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val targets = targetsInRange.characters.map(_.id)
    val masterThroneOpt = parentCharacter.state.abilities.collectFirst { case a: MasterThrone => a }
    if(masterThroneOpt.isEmpty) return gameState
    val masterThrone = masterThroneOpt.get
    val damage = Damage(DamageType.Magical, masterThrone.collectedEnergy / targets.size)
    targets.foldLeft(gameState)((acc, cid) => {
      hitAndDamageCharacter(cid, damage)(random, acc)
    })
  }
}
