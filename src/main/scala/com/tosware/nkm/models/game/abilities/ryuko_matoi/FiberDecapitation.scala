package com.tosware.nkm.models.game.abilities.ryuko_matoi

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.Ability.AbilityId
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._

import scala.util.Random

object FiberDecapitation {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Fiber Decapitation",
      abilityType = AbilityType.Normal,
      description =
        """Cut through selected enemy, decreasing his physical defense by {physicalDefenseDecrease},
          |dealing {damage} physical damage and landing {targetCellOffset} tiles behind him.
          |
          |Range: {Range}""".stripMargin,
      variables = NkmConf.extract("abilities.ryukoMatoi.fiberDecapitation"),
    )
}

case class FiberDecapitation(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId) with UsableOnCharacter {
  override val metadata = FiberDecapitation.metadata
  override val state = AbilityState(parentCharacterId)
  override def rangeCellCoords(implicit gameState: GameState) = ???

  override def targetsInRange(implicit gameState: GameState) = ???

  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState) = ???
}
