package com.tosware.nkm.models.game.abilities.nibutani_shinka

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.ability.Ability.AbilityId
import com.tosware.nkm.models.game.character.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.ability.{Ability, AbilityMetadata, AbilityType}
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
      variables = NkmConf.extract("abilities.nibutaniShinka.mabinogion"),
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

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState = {
    e match {
      case GameEvent.TurnFinished(_, _, _, _) =>
        val characterIdThatTookAction = gameState.gameLog.characterThatTookActionInTurn(e.turn.number).get
        if (characterIdThatTookAction != parentCharacterId) return gameState
        ???
      case _ => gameState
    }
  }
}
