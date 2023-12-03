package com.tosware.nkm.models.game.abilities.satou_kazuma

import com.softwaremill.quicklens.*
import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.abilities.satou_kazuma.Steal.stolenDatasKey
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}
import com.tosware.nkm.models.game.hex.HexCoordinates

import scala.util.Random
import spray.json.*

object Steal extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Steal",
      abilityType = AbilityType.Ultimate,
      description =
        """Steal pants from an enemy for {duration}t, zeroing their physical and magical defense and adding them to themself.
          |
          |Range: circular, {range}""".stripMargin,
    )
  def stolenDatasKey: String = "stolenDatas"
}

final case class StolenData(
    id: String,
    stolenFrom: CharacterId,
    willBeRestoredAtPhase: Int,
    stolenPhysicalDefense: Int,
    stolenMagicalDefense: Int,
)

case class Steal(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId)
    with UsableOnCharacter
    with GameEventListener {
  override val metadata: AbilityMetadata = Steal.metadata

  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    parentCell.fold(Set.empty[HexCoordinates])(_.coordinates.getCircle(metadata.variables("range")).whereExists)

  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords.whereSeenEnemiesOfC(parentCharacterId)

  private def getStolenDatas()(implicit random: Random, gameState: GameState) =
    state.variables.get(stolenDatasKey).map(_.parseJson.convertTo[Set[StolenData]]).getOrElse(Set.empty)

  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val targetCharacter = gameState.characterById(target)
    val stolenData = StolenData(
      randomUUID(),
      target,
      gameState.phase.number + metadata.variables("duration"),
      targetCharacter.state.purePhysicalDefense,
      targetCharacter.state.pureMagicalDefense,
    )

    gameState
      .setAbilityEnabled(abilityId, newEnabled = true)
      .setAbilityVariable(id, stolenDatasKey, (getStolenDatas() + stolenData).toJson.toString)
      .updateCharacter(parentCharacterId)(_
        .modify(_.state.purePhysicalDefense).using(_ + stolenData.stolenPhysicalDefense)
        .modify(_.state.pureMagicalDefense).using(_ + stolenData.stolenMagicalDefense)).updateCharacter(target)(_
        .modify(_.state.purePhysicalDefense).setTo(0)
        .modify(_.state.pureMagicalDefense).setTo(0))
  }

  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState = e match {
    case GameEvent.PhaseFinished(_, phase, _, _) =>
      getStolenDatas().filter(_.willBeRestoredAtPhase == phase.number + 1).foldLeft(gameState) { (acc, stolenData) =>
        val newStolenDatas = getStolenDatas() - stolenData

        acc
          .setAbilityVariable(id, stolenDatasKey, newStolenDatas.toJson.toString)
          .setAbilityEnabled(abilityId, newEnabled = newStolenDatas.nonEmpty)
          .updateCharacter(parentCharacterId)(_
            .modify(_.state.purePhysicalDefense).using(_ - stolenData.stolenPhysicalDefense)
            .modify(_.state.pureMagicalDefense).using(_ - stolenData.stolenMagicalDefense))
          .updateCharacter(stolenData.stolenFrom)(_
            .modify(_.state.purePhysicalDefense).using(_ + stolenData.stolenPhysicalDefense)
            .modify(_.state.pureMagicalDefense).using(_ + stolenData.stolenPhysicalDefense))
      }
    case _ => gameState
  }
}
