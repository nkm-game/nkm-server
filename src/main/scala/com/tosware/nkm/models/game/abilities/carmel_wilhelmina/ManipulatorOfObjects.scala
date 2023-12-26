package com.tosware.nkm.models.game.abilities.carmel_wilhelmina

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.effects.{ManipulatorOfObjectsImmunity, Snare}
import com.tosware.nkm.models.game.event.GameEvent.CharacterBasicAttacked
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}
import com.tosware.nkm.models.game.game_state.GameState

import scala.util.Random

object ManipulatorOfObjects extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Manipulator of Objects",
      alternateName = "万条の仕手 (Banjō no Shite)",
      abilityType = AbilityType.Passive,
      description = """Basic attacks snare enemies for {duration}t.
                      |This effect cannot be applied to the same character for {effectTimeout}t.""".stripMargin,
      relatedEffectIds = Seq(Snare.metadata.id, ManipulatorOfObjectsImmunity.metadata.id),
    )
}

case class ManipulatorOfObjects(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId) with GameEventListener {
  override val metadata = ManipulatorOfObjects.metadata

  private def tryToSnare(targetCharacterId: CharacterId)(implicit random: Random, gameState: GameState): GameState =
    if (gameState.characterById(targetCharacterId).state.effects.ofType[effects.ManipulatorOfObjectsImmunity].nonEmpty)
      gameState
    else {
      val snareEffect = effects.Snare(
        randomUUID(),
        metadata.variables("duration"),
      )
      val immunityEffect = effects.ManipulatorOfObjectsImmunity(
        randomUUID(),
        metadata.variables("effectTimeout"),
      )
      gameState
        .addEffect(targetCharacterId, snareEffect)(random, id)
        .addEffect(targetCharacterId, immunityEffect)(random, id)
    }

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case CharacterBasicAttacked(_, _, _, _, characterId, targetCharacterId) =>
        if (parentCharacterId == characterId) {
          tryToSnare(targetCharacterId)
        } else gameState
      case _ => gameState
    }
}
