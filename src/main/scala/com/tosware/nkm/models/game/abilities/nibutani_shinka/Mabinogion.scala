package com.tosware.nkm.models.game.abilities.nibutani_shinka

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.character.StatType
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.HexCoordinates

import scala.util.Random

object Mabinogion extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Mabinogion",
      abilityType = AbilityType.Passive,
      description =
        """At the end of your turn heal all nearby characters for {heal} HP.
          |Additionally allies gain up to {shield} shield.
          |
          |This ability can be enchanted:
          |- heal only allies
          |- heal is tripled
          |- shield is tripled
          |- all friends in range gain {enchantedSpeed} Speed for 1t
          |
          |Radius: circular, {radius}""".stripMargin,
    )
}

case class Mabinogion(
    abilityId: AbilityId,
    parentCharacterId: CharacterId,
) extends Ability(abilityId) with GameEventListener {
  override val metadata: AbilityMetadata = Mabinogion.metadata
  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    defaultCircleRange(metadata.variables("radius"))
  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    if (isEnchanted)
      rangeCellCoords.whereFriendsOfC(parentCharacterId)
    else
      rangeCellCoords.whereCharacters
  private def healAmount(implicit gameState: GameState): Int = {
    val baseHeal = metadata.variables("heal")
    if (isEnchanted) baseHeal * 3
    else baseHeal
  }
  private def shieldAmount(implicit gameState: GameState): Int = {
    val baseShield = metadata.variables("shield")
    if (isEnchanted) baseShield * 3
    else baseShield
  }
  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case GameEvent.TurnFinished(_, _) =>
        val characterIdThatTookAction = gameState.gameLog.characterThatTookActionInTurn(e.context.turn.number).get
        if (characterIdThatTookAction != parentCharacterId) return gameState
        val healGs = targetsInRange.characters.foldLeft(gameState)((acc, target) =>
          acc.heal(target.id, healAmount)(random, id)
        )
        val shieldGs = targetsInRange.whereFriendsOfC(parentCharacterId).characters.foldLeft(healGs)((acc, target) =>
          acc.setShield(target.id, Math.max(target.state.shield, shieldAmount))(random, id)
        )
        if (isEnchanted)
          targetsInRange.whereFriendsOfC(parentCharacterId).characters.foldLeft(shieldGs) { (acc, target) =>
            // Workaround because cooldown is decreased immediately for parent
            val duration = if (target.id == parentCharacterId) 2 else 1

            acc.addEffect(
              target.id,
              effects.StatBuff(randomUUID(), duration, StatType.Speed, metadata.variables("enchantedSpeed")),
            )(random, id)
          }
        else shieldGs
      case _ => gameState
    }
}
