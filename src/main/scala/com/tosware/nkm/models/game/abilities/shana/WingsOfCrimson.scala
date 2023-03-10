package com.tosware.nkm.models.game.abilities.shana

import com.tosware.nkm._
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.ability._
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}

import scala.util.Random

object WingsOfCrimson {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Wings of Crimson",
      alternateName = "紅蓮の双翼 (Guren no Sōyoku)",
      abilityType = AbilityType.Passive,
      description =
        """After receiving damage, character unfolds their wings,
           gaining {bonusSpeed} speed and gaining the ability to fly for {duration}t.
          """.stripMargin,
      variables = NkmConf.extract("abilities.shana.wingsOfCrimson"),
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
        if(characterId != parentCharacter.id) return gameState
        gameState
      case _ =>
        gameState
    }
}
