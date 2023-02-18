package com.tosware.nkm.models.game.abilities.ryuko_matoi

import com.tosware.nkm.{NkmConf, NkmUtils}
import com.tosware.nkm.models.game.ability.Ability.AbilityId
import com.tosware.nkm.models.game.character.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.ability.{Ability, AbilityMetadata, AbilityType}
import com.tosware.nkm.models.game.character.StatType
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}

import scala.util.Random

object ScissorBlade {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "ScissorBlade",
      abilityType = AbilityType.Passive,
      description =
        """This character's basic attacks decrease physical defense of enemies by {physicalDefenseDecrease} for {duration}t.
          |This effect is applied before attack and can stack.""".stripMargin,
      variables = NkmConf.extract("abilities.ryukoMatoi.scissorBlade"),
      relatedEffectIds = Seq(effects.StatNerf.metadata.id)
    )
}

case class ScissorBlade(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId) with GameEventListener {
  override val metadata = ScissorBlade.metadata

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case GameEvent.CharacterPreparedToAttack(_, _, _, _, characterId, targetCharacterId) =>
        if(characterId != parentCharacterId) gameState
        else {
          val effect = effects.StatNerf(NkmUtils.randomUUID(), metadata.variables("duration"), StatType.PhysicalDefense, metadata.variables("physicalDefenseDecrease"))
          gameState.addEffect(targetCharacterId, effect)(random, id)
        }
      case _ => gameState
    }
}
