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
          |This effect can stack and is permanent.
          |Each stack deals {poisonDamage} true damage at the end of turn.
          |After applying {poisonStacksToDie} stacks target dies immediately.""".stripMargin,
      variables = NkmConf.extract("abilities.akame.murasame"),
    )
}

case class Murasame(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId) with GameEventListener {
  override val metadata = Murasame.metadata

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState) = ???
}
