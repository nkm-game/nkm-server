package com.tosware.nkm.models.game.abilities.llenn

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.ability.Ability.AbilityId
import com.tosware.nkm.models.game.character.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.ability.{Ability, AbilityMetadata, AbilityType}
import com.tosware.nkm.models.game.character.StatType
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}

import scala.util.Random

object PChan {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "P-Chan",
      abilityType = AbilityType.Passive,
      description = "Character permanently gains {speedIncrease} speed with every death of a friendly character.",
      variables = NkmConf.extract("abilities.llenn.pChan"),
    )
}

case class PChan
(
  abilityId: AbilityId,
  parentCharacterId: CharacterId,
) extends Ability(abilityId, parentCharacterId) with GameEventListener {
  override val metadata = PChan.metadata

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState = {
    e match {
      case GameEvent.CharacterDied(_, _, _, _, characterId) =>
        if(parentCharacter.isFriendForC(characterId)) {
          gameState.setStat(parentCharacterId, StatType.Speed, parentCharacter.state.pureSpeed + metadata.variables("speedIncrease"))(random, id)
        }
        else gameState
      case _ => gameState
    }
  }
}
