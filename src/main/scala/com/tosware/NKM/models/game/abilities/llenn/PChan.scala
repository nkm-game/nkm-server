package com.tosware.NKM.models.game.abilities.llenn

import com.tosware.NKM.models.game.Ability.AbilityId
import com.tosware.NKM.models.game.NKMCharacter.CharacterId
import com.tosware.NKM.models.game._

object PChan {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "P-Chan",
      abilityType = AbilityType.Passive,
      description = "This character permanently gains speed with every death of a friendly character.",
    )
}

case class PChan
(
  abilityId: AbilityId,
  parentCharacterId: CharacterId,
) extends Ability(abilityId) with GameEventListener {
  override val metadata = PChan.metadata
  override val state = AbilityState(parentCharacterId)

  override def onEvent(e: GameEvent.GameEvent)(implicit gameState: GameState): GameState = {
    e match {
      case GameEvent.CharacterDied(characterId) =>
        if(parentCharacter.isFriendFor(characterId)) {
          // TODO: increase speed
          ???
        }
        else gameState
      case _ => gameState
    }
  }
}
