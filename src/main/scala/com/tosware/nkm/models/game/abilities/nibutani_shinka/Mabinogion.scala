package com.tosware.nkm.models.game.abilities.nibutani_shinka

import com.tosware.nkm._
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.ability._
import com.tosware.nkm.models.game.character.StatType
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}
import com.tosware.nkm.models.game.hex.HexCoordinates

import scala.util.Random

object Mabinogion {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Mabinogion",
      abilityType = AbilityType.Passive,
      description =
        """At the end of this character's turn, heal all nearby characters in radius {radius} for {heal} HP.
          |Additionally, friendly characters gain up to {shield} shield.
          |
          |This ability can be enchanted:
          |- heal only friendly characters
          |- heal is tripled
          |- shield is tripled
          |- all friends in range gain {enchantedSpeed} speed for one phase
          |""".stripMargin,
      variables = NkmConf.extract("abilities.nibutani_shinka.mabinogion"),
    )
}

case class Mabinogion
(
  abilityId: AbilityId,
  parentCharacterId: CharacterId,
) extends Ability(abilityId, parentCharacterId) with GameEventListener {
  override val metadata = Mabinogion.metadata

  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    parentCell.get.getArea(metadata.variables("radius")).toCoords

  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    if(isEnchanted)
      rangeCellCoords.whereFriendsOfC(parentCharacterId)
    else
      rangeCellCoords.whereCharacters

  def healAmount(implicit gameState: GameState): Int = {
    val baseHeal = metadata.variables("heal")
    if(isEnchanted) baseHeal * 3
    else baseHeal
  }

  def shieldAmount(implicit gameState: GameState): Int = {
    val baseShield = metadata.variables("shield")
    if(isEnchanted) baseShield * 3
    else baseShield
  }

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState = {
    e match {
      case GameEvent.TurnFinished(_, _, _, _) =>
        val characterIdThatTookAction = gameState.gameLog.characterThatTookActionInTurn(e.turn.number).get
        if (characterIdThatTookAction != parentCharacterId) return gameState
        val healGs = targetsInRange.characters.foldLeft(gameState)((acc, target) =>
          acc.heal(target.id, healAmount)(random, id)
        )
        val shieldGs = targetsInRange.whereFriendsOfC(parentCharacterId).characters.foldLeft(healGs)((acc, target) =>
          acc.setShield(target.id, Math.max(target.state.shield, shieldAmount))(random, id)
        )
        if(isEnchanted)
          targetsInRange.whereFriendsOfC(parentCharacterId).characters.foldLeft(shieldGs)((acc, target) =>
            acc.addEffect(target.id, effects.StatBuff(randomUUID(), 1, StatType.Speed, metadata.variables("enchantedSpeed")))(random, id)
          )
        else shieldGs
      case _ => gameState
    }
  }
}
