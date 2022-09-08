package com.tosware.nkm.models.game.abilities.ryuko_matoi

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.Ability.AbilityId
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._

import scala.util.Random

object ScissorBlade {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "ScissorBlade",
      abilityType = AbilityType.Passive,
      description =
        """Basic attacks of this character decrease physical defense of enemies by {physicalDefenseDecrease} for {duration}t.
          |This effect is applied before attack and can stack.""".stripMargin,
      variables = NkmConf.extract("abilities.ryukoMatoi.scissorBlade"),
    )
}

case class ScissorBlade(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId) with GameEventListener {
  override val metadata = ScissorBlade.metadata


  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState = ???
}
