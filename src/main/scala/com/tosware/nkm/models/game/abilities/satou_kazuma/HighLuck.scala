package com.tosware.nkm.models.game.abilities.satou_kazuma

import com.tosware.nkm.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}
import com.tosware.nkm.models.game.game_state.GameState

import scala.util.Random

object HighLuck extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "High Luck",
      abilityType = AbilityType.Passive,
      description =
        """{criticalStrikePercent}% chance for a critical strike when dealing damage.
          |Critical strike deals double damage.""".stripMargin,
    )
}

case class HighLuck(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId)
    with GameEventListener {
  override val metadata: AbilityMetadata = HighLuck.metadata
  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case GameEvent.DamagePrepared(damagePreparedId, _, _, causedById, _, damage) =>
        val causedByCharacterIdOpt = gameState.backtrackCauseToCharacterId(causedById)
        causedByCharacterIdOpt.fold(gameState) { causedByCharacterId =>
          if (causedByCharacterId != parentCharacterId) return gameState
          val isCritical: Boolean = random.between(0f, 100f) < (metadata.variables("criticalStrikePercent"))
          if (!isCritical) return gameState
          gameState.amplifyDamage(damagePreparedId, damage.amount)(random, id)
        }
      case _ =>
        gameState
    }
}
