package com.tosware.nkm.models.game.game_state.extensions

import com.softwaremill.quicklens.*
import com.tosware.nkm.*
import com.tosware.nkm.models.game.event.*
import com.tosware.nkm.models.game.event.GameEvent.*
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.*
import com.tosware.nkm.models.game.hex_effect.*

import scala.util.Random

object GameStateHexCellEffectUtils extends GameStateHexCellEffectUtils
trait GameStateHexCellEffectUtils {
  implicit class GameStateHexCellEffectUtils(gs: GameState) {
    def addHexCellEffect(coordinates: HexCoordinates, hexCellEffect: HexCellEffect)(
        implicit
        random: Random,
        causedById: String,
    ): GameState = {
      val ngs = gs.updateHexCell(coordinates)(_.addEffect(hexCellEffect))
        .modify(_.hexCellEffectStates)
        .using(hes => hes.updated(hexCellEffect.id, HexCellEffectState(hexCellEffect.initialCooldown)))

      val causedByPlayer = gs.backtrackCauseToPlayerId(causedById)(ngs)

      val createEffectAddedEvent =
        EffectAddedToCell(randomUUID(), gs.phase, gs.turn, causedById, hexCellEffect.id, coordinates)

      causedByPlayer match {
        case Some(player) if hexCellEffect.metadata.name == HexCellEffectName.MarkOfTheWind =>
          ngs.logAndHideEvent(
            createEffectAddedEvent,
            Seq(player),
            RevealCondition.RelatedTrapRevealed(hexCellEffect.id),
          )
        case _ =>
          ngs.logEvent(createEffectAddedEvent)
      }
    }

    def removeHexCellEffects(heids: Seq[HexCellEffectId])(implicit random: Random, causedById: String): GameState =
      heids.foldLeft(gs) { case (acc, eid) => acc.removeHexCellEffect(eid) }

    private def hexCellEffectCoordsOpt(heid: HexCellEffectId): Option[HexCoordinates] =
      gs
        .hexCellEffectByIdOpt(heid)
        .flatMap(_.parentCell(gs))
        .map(_.coordinates)

    def removeHexCellEffect(heid: HexCellEffectId)(implicit random: Random, causedById: String): GameState = {
      val ngs =
        (for {
          hexCellEffect <- gs.hexCellEffectByIdOpt(heid)
          if hexCellEffect.metadata.name == HexCellEffectName.MarkOfTheWind
        } yield gs.reveal(RevealCondition.RelatedTrapRevealed(heid))) getOrElse gs
      hexCellEffectCoordsOpt(heid) match {
        case Some(coords) =>
          ngs.updateHexCell(coords)(_.removeEffect(heid))
            .modify(_.hexCellEffectStates).using(hes => hes.removed(heid))
            .logEvent(EffectRemovedFromCell(randomUUID(), gs.phase, gs.turn, causedById, heid))
        case None => ngs
      }
    }
  }
}
