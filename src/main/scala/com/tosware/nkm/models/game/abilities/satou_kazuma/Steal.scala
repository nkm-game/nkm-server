package com.tosware.nkm.models.game.abilities.satou_kazuma

import com.softwaremill.quicklens.*
import com.tosware.nkm.*
import com.tosware.nkm.models.game.abilities.satou_kazuma.Steal.stolenDataKey
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.event.{GameEvent, GameEventListener}
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.HexCoordinates
import spray.json.*

import scala.util.Random

object Steal extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Steal",
      abilityType = AbilityType.Ultimate,
      description =
        """Steal pants from an enemy for {duration}t, zeroing their physical and magical defense and adding them to themself.
          |
          |Range: circular, {range}""".stripMargin,
      targetsMetadata = Seq(AbilityTargetMetadata.SingleCharacter),
    )
  def stolenDataKey: String = "stolenData"
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
    with Usable
    with GameEventListener {
  override val metadata: AbilityMetadata = Steal.metadata
  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    parentCell.fold(Set.empty[HexCoordinates])(_.coordinates.getCircle(metadata.variables("range")).whereExists)
  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords.whereSeenEnemiesOfC(parentCharacterId)
  private def stolenDataSet(implicit gameState: GameState) =
    state.variables.get(stolenDataKey).map(_.parseJson.convertTo[Set[StolenData]]).getOrElse(Set.empty)
  override def use(useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val target = useData.firstAsCharacterId
    val targetCharacter = gameState.characterById(target)
    val newStolenData = StolenData(
      randomUUID(),
      target,
      gameState.phase.number + metadata.variables("duration"),
      targetCharacter.state.purePhysicalDefense,
      targetCharacter.state.pureMagicalDefense,
    )
    gameState
      .setAbilityEnabled(abilityId, newEnabled = true)
      .setAbilityVariable(id, stolenDataKey, (stolenDataSet + newStolenData).toJson.toString)
      .updateCharacter(parentCharacterId)(_
        .modify(_.state.purePhysicalDefense).using(_ + newStolenData.stolenPhysicalDefense)
        .modify(_.state.pureMagicalDefense).using(_ + newStolenData.stolenMagicalDefense)).updateCharacter(target)(_
        .modify(_.state.purePhysicalDefense).setTo(0)
        .modify(_.state.pureMagicalDefense).setTo(0))
  }
  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState = e match {
    case GameEvent.PhaseFinished(_, phase, _, _) =>
      stolenDataSet.filter(_.willBeRestoredAtPhase == phase.number + 1).foldLeft(gameState) { (acc, stolenData) =>
        val newStolenDatas = stolenDataSet - stolenData

        acc
          .setAbilityVariable(id, stolenDataKey, newStolenDatas.toJson.toString)
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
  override def useChecks(implicit useData: UseData, gameState: GameState): Set[UseCheck] =
    super.useChecks ++ characterBaseUseChecks(useData.firstAsCharacterId)
}
