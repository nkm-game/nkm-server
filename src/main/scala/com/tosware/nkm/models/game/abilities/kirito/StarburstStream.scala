package com.tosware.nkm.models.game.abilities.kirito

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.kirito.StarburstStream.doubleAttackEnabledKey
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.event.GameEvent.CharacterBasicAttacked
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}
import com.tosware.nkm.models.game.hex.{HexCoordinates, SearchFlag}
import spray.json.*

import scala.util.Random

object StarburstStream extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Starburst Stream",
      abilityType = AbilityType.Ultimate,
      description =
        """Attack an enemy {attackTimes} times.
          |Each hit deals {damage} true damage.
          |After using this ability, you can permanently basic attack {basicAttacksPerTurn} times per turn.
          |
          |Range: linear, {range}""".stripMargin,
    )

  val doubleAttackEnabledKey = "doubleAttackEnabled"
}

case class StarburstStream(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId, parentCharacterId)
    with UsableOnCharacter
    with GameEventListener {
  override val metadata = StarburstStream.metadata

  def doubleAttackEnabled(implicit gameState: GameState): Boolean =
    state.variables.get(doubleAttackEnabledKey)
      .map(_.parseJson.convertTo[Boolean])
      .getOrElse(false)

  private def setDoubleAttackEnabled()(implicit random: Random, gameState: GameState): GameState =
    gameState.setAbilityVariable(id, doubleAttackEnabledKey, true.toJson.toString)

  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    parentCell.get.getArea(metadata.variables("range"), Set(SearchFlag.StraightLine)).toCoords

  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords.whereEnemiesOfC(parentCharacterId)

  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val enabledGs = setDoubleAttackEnabled()

    val times = metadata.variables("attackTimes")
    (0 to times).foldLeft(enabledGs) { (acc, _) =>
      hitAndDamageCharacter(target, Damage(DamageType.True, metadata.variables("damage")))(random, acc)
    }
  }

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case CharacterBasicAttacked(_, _, _, _, characterId, _) =>
        if (characterId != parentCharacterId) return gameState
        if (!doubleAttackEnabled) return gameState
        val timesCharacterAttackedThisTurn = gameState.gameLog.events
          .inTurn(gameState.turn.number)
          .ofType[GameEvent.CharacterBasicAttacked]
          .ofCharacter(parentCharacterId)
          .size
        if (timesCharacterAttackedThisTurn >= metadata.variables("basicAttacksPerTurn")) return gameState
        gameState.refreshBasicAttack(parentCharacterId)(random, id)
      case _ => gameState
    }
}
