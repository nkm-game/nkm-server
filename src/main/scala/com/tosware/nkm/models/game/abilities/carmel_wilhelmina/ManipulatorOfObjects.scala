package com.tosware.nkm.models.game.abilities.carmel_wilhelmina

import com.tosware.nkm.{NkmConf, NkmUtils}
import com.tosware.nkm.models.game.Ability.AbilityId
import com.tosware.nkm.models.game.GameEvent.CharacterBasicAttacked
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.effects.{ManipulatorOfObjectsImmunity, Snare}

import scala.util.Random

object ManipulatorOfObjects {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Manipulator of Objects",
      alternateName = "万条の仕手",
      abilityType = AbilityType.Passive,
      description = """This character's basic attacks snare enemies for {duration}t.
      |This effect cannot be added on the same enemy for {effectTimeout}t.""".stripMargin,
      variables = NkmConf.extract("abilities.carmelWilhelmina.manipulatorOfObjects"),
      relatedEffectIds = Seq(Snare.metadata.id, ManipulatorOfObjectsImmunity.metadata.id),
    )
}

case class ManipulatorOfObjects(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId) with GameEventListener {
  override val metadata = ManipulatorOfObjects.metadata

  private def tryToSnare(targetCharacterId: CharacterId)(implicit random: Random, gameState: GameState): GameState = {
    if(gameState.characterById(targetCharacterId).state.effects.ofType[effects.ManipulatorOfObjectsImmunity].nonEmpty)
      gameState
    else {
      val snareEffect = effects.Snare(
        NkmUtils.randomUUID(),
        metadata.variables("duration"),
      )
      val immunityEffect = effects.ManipulatorOfObjectsImmunity(
        NkmUtils.randomUUID(),
        metadata.variables("effectTimeout"),
      )
      gameState
        .addEffect(targetCharacterId, snareEffect)(random, id)
        .addEffect(targetCharacterId, immunityEffect)(random, id)
    }
  }

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case CharacterBasicAttacked(_, characterId, targetCharacterId) =>
        if(parentCharacterId == characterId) {
          tryToSnare(targetCharacterId)
        } else gameState
      case _ => gameState
    }
}
