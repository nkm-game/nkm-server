package com.tosware.nkm.models.game.abilities.akame

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.Ability.AbilityId
import com.tosware.nkm.models.game.GameEvent.{AbilityHitCharacter, CharacterBasicAttacked, EffectAddedToCharacter}
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.effects.MurasamePoison
import com.tosware.nkm.models.game.hex.NkmUtils
import com.tosware.nkm.models.{Damage, DamageType}

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
      relatedEffectIds = Seq(MurasamePoison.metadata.id),
    )
}

case class Murasame(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId) with GameEventListener {
  override val metadata = Murasame.metadata

  private def killCharacterIfFullyStacked(targetCharacterId: CharacterId)(implicit random: Random, gameState: GameState): GameState = {
    val numberOfStacks = gameState
      .characterById(targetCharacterId).get
      .state
      .effects
      .count(e => e.metadata.name == CharacterEffectName.MurasamePoison)

    if(numberOfStacks >= metadata.variables("poisonStacksToDie")) {
      gameState.executeCharacter(targetCharacterId)(random, id)
    } else gameState
  }

  private def applyPoison(targetCharacterId: CharacterId)(implicit random: Random, gameState: GameState): GameState = {
    val poisonEffect = effects.Poison(
      NkmUtils.randomUUID(),
      Int.MaxValue,
      Damage(DamageType.True, metadata.variables("poisonDamage")),
      MurasamePoison.metadata,
    )
    gameState.addEffect(targetCharacterId, poisonEffect)(random, id)
  }

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState) =
    e match {
      case CharacterBasicAttacked(_, characterId, targetCharacterId) =>
        if(parentCharacterId == characterId) {
          applyPoison(targetCharacterId)
        } else gameState
      case AbilityHitCharacter(_, abilityId, targetCharacterId) =>
        if(parentCharacterId == gameState.abilityById(abilityId).get.parentCharacter.id) {
          applyPoison(targetCharacterId)
        } else gameState
      case EffectAddedToCharacter(_, _, targetCharacterId) =>
        killCharacterIfFullyStacked(targetCharacterId)
      case _ => gameState
    }
}
