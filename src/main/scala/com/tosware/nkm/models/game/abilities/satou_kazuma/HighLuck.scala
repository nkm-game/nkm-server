package com.tosware.nkm.models.game.abilities.satou_kazuma

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.ability.Ability.AbilityId
import com.tosware.nkm.models.game.ability._
import com.tosware.nkm.models.game.character.NkmCharacter.CharacterId
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}

import scala.util.Random

object HighLuck {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "High Luck",
      abilityType = AbilityType.Passive,
      description =
        """Character has a {criticalStrikePercent}% chance to strike critically when dealing damage.
          |Critical strike deals double damage.
          |""".stripMargin,
      variables = NkmConf.extract("abilities.satou_kazuma.highLuck"),
    )
}

case class HighLuck(abilityId: AbilityId, parentCharacterId: CharacterId)
  extends Ability(abilityId, parentCharacterId)
    with GameEventListener {
  override val metadata: AbilityMetadata = HighLuck.metadata

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState = {
    e match {
      case _ =>
        gameState
    }
  }
}
