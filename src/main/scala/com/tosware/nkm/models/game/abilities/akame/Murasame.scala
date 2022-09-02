package com.tosware.nkm.models.game.abilities.akame

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.Ability.AbilityId
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._

import scala.util.Random

object Murasame {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Murasame",
      abilityType = AbilityType.Passive,
      description =
        """Character applies Poison effect on basic attack or ability hit.
          |This effect can stack.
          |Each stack deals 1 true damage.
          |After applying 7 stacks target dies immediately.
          |Effect remains till the end of the game.""".stripMargin,
      variables = NkmConf.extract("abilities.akame.murasame"),
    )
}

case class Murasame(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId) with GameEventListener {
  override val metadata = Murasame.metadata
  override val state = AbilityState(parentCharacterId)

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState) = ???
}
