package com.tosware.nkm.models.game.abilities.shana

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.character.StatType
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}

import scala.util.Random

object WingsOfCrimson extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Wings of Crimson",
      alternateName = "紅蓮の双翼 (Guren no Sōyoku)",
      abilityType = AbilityType.Passive,
      description =
        """After receiving damage, character unfolds their wings,
           gaining {bonusSpeed} speed and gaining the ability to fly for {duration}t.
          """.stripMargin,
      relatedEffectIds = Seq(
        effects.Fly.metadata.id,
        effects.StatBuff.metadata.id,
      ),
    )
}

case class WingsOfCrimson(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId, parentCharacterId)
    with GameEventListener {
  override val metadata: AbilityMetadata = WingsOfCrimson.metadata

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case GameEvent.CharacterDamaged(_, _, _, _, characterId, _) =>
        if (characterId != parentCharacterId) return gameState
        gameState
          .addEffect(parentCharacterId, effects.Fly(randomUUID(), metadata.variables("duration")))(random, id)
          .addEffect(
            parentCharacterId,
            effects.StatBuff(
              randomUUID(),
              metadata.variables("duration"),
              StatType.Speed,
              metadata.variables("bonusSpeed"),
            ),
          )(random, id)
      case _ =>
        gameState
    }
}
