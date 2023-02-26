package com.tosware.nkm.models.game.abilities.ayatsuji_ayase

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.ability.Ability.AbilityId
import com.tosware.nkm.models.game.ability._
import com.tosware.nkm.models.game.character.NkmCharacter.CharacterId
import com.tosware.nkm.models.game.effects.StatBuff
import com.tosware.nkm.models.game.event.GameEvent._
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}

import scala.util.Random

object SamuraisSwiftness {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Samurai's Swiftness",
      abilityType = AbilityType.Passive,
      description =
        "Dealing damage by this character gives a {speedPercent}% speed buff in their next turn",
      variables = NkmConf.extract("abilities.ayatsuji_ayase.samuraisSwiftness"),
      relatedEffectIds = Seq(StatBuff.metadata.id),
    )
}

case class SamuraisSwiftness(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId) with GameEventListener {
  override val metadata = SamuraisSwiftness.metadata

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case CharacterDamaged(_, _, _, _, characterId, _) =>
        ???
      case _ => gameState
    }
}
