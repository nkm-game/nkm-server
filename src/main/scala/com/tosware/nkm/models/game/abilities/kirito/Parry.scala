package com.tosware.nkm.models.game.abilities.kirito

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.Ability.AbilityId
import com.tosware.nkm.models.game.GameEvent.CharacterPreparedToAttack
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._

import scala.util.Random

object Parry {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Parry",
      abilityType = AbilityType.Passive,
      description = "Character has a {dodgeChancePercent}% chance to block basic attack of an enemy",
      variables = NkmConf.extract("abilities.kirito.parry"),
      relatedEffectIds = Seq(effects.Block.metadata.id),
    )
}

case class Parry(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId) with GameEventListener {
  override val metadata = Parry.metadata

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case CharacterPreparedToAttack(_, _, _, _, _, targetCharacterId) =>
        if(targetCharacterId != parentCharacterId) return gameState
        val dodged: Boolean = random.between(0f, 100f) < (metadata.variables("dodgeChancePercent"))
        if(!dodged) return gameState
        gameState.addEffect(parentCharacterId, effects.Block(randomUUID(), 1))(random, id)
      case _ => gameState
    }
}
