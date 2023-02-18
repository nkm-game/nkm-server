package com.tosware.nkm.models.game.abilities.kirito

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.ability.Ability.AbilityId
import com.tosware.nkm.models.game.event.GameEvent.{AbilityUsedOnCharacter, CharacterBasicAttacked}
import com.tosware.nkm.models.game.character.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.ability.{Ability, AbilityMetadata, AbilityType, UsableOnCharacter, UseData}
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}
import com.tosware.nkm.models.game.hex.{HexCoordinates, SearchFlag}

import scala.util.Random

object StarburstStream {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Starburst Stream",
      abilityType = AbilityType.Ultimate,
      description =
        """Attack the enemy {attackTimes} times.
          |Every hit deals {damage} true damage.
          |After using this ability, you can permanently basic attack {basicAttacksPerTurn} times per turn.
          |
          |Range: linear, {range}""".stripMargin,
      variables = NkmConf.extract("abilities.kirito.starburstStream"),
    )
}

case class StarburstStream(abilityId: AbilityId, parentCharacterId: CharacterId)
  extends Ability(abilityId, parentCharacterId)
    with UsableOnCharacter
    with GameEventListener
{
  override val metadata = StarburstStream.metadata

  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    parentCell.get.getArea(metadata.variables("range"), Set(SearchFlag.StraightLine)).toCoords

  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords.whereEnemiesOfC(parentCharacterId)

  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val times = metadata.variables("attackTimes")
    (0 to times).foldLeft(gameState)((acc, _) => {
      hitAndDamageCharacter(target, Damage(DamageType.True, metadata.variables("damage")))(random, acc)
    })
  }

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case CharacterBasicAttacked(_, _, _, _, characterId, _) =>
        if(characterId != parentCharacterId) return gameState
        val abilityWasUsed = gameState.gameLog.events.ofType[AbilityUsedOnCharacter].ofAbility(id).nonEmpty
        if(!abilityWasUsed) return gameState
        val timesCharacterAttackedThisTurn = gameState.gameLog.events
          .inTurn(gameState.turn.number)
          .ofType[GameEvent.CharacterBasicAttacked]
          .ofCharacter(parentCharacterId)
          .size
        if(timesCharacterAttackedThisTurn >= metadata.variables("basicAttacksPerTurn")) return gameState
        gameState.refreshBasicAttack(parentCharacterId)(random, id)
      case _ => gameState
    }
}
