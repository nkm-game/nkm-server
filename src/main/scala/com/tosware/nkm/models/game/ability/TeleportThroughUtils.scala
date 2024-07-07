package com.tosware.nkm.models.game.ability

import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.*
import com.tosware.nkm.{CharacterId, UseCheck}
import com.tosware.nkm.*

object TeleportThroughUtils {
  def rangeCellCoords(a: Ability)(implicit gameState: GameState): Set[HexCoordinates] =
    a.parentCellOpt.fold(Set.empty[HexCell])(c =>
      c.getArea(
        a.metadata.variables("range"),
        Set(SearchFlag.StopAtWalls, SearchFlag.StopAfterEnemies, SearchFlag.StraightLine),
        friendlyPlayerIdOpt = Some(a.parentCharacter.owner.id),
      )
    ).toCoords

  def targetsInRange(a: Ability, tpOffset: Int)(implicit
      gameState: GameState
  ): Set[HexCoordinates] =
    rangeCellCoords(a)
      .whereSeenEnemiesOfC(a.parentCharacter.id)
      .filter(targetCoordinates =>
        {
          for {
            pCell <- a.parentCellOpt
            targetDirection: HexDirection <- pCell.coordinates.getDirection(targetCoordinates)
            tpCoords: HexCoordinates <- Some(tpCoords(targetCoordinates, targetDirection, tpOffset))
            tpCell: HexCell <- tpCoords.toCellOpt(gameState)
            isFreeToStand: Boolean <- Some(tpCell.looksFreeToStand(a.parentCharacter.id))
          } yield isFreeToStand
        }.getOrElse(false)
      )

  private def tpCoords(from: HexCoordinates, direction: HexDirection, tpOffset: Int) =
    from.getInDirection(direction, tpOffset)

  def tpCoordsOpt(a: Ability, targetId: CharacterId, tpOffset: Int)(implicit
      gameState: GameState
  ): Option[HexCoordinates] = for {
    pCell <- a.parentCellOpt
    targetCharacter <- gameState.characterByIdOpt(targetId)
    targetCoordinates <- targetCharacter.parentCellOpt.map(_.coordinates)
    targetDirection: HexDirection <- pCell.coordinates.getDirection(targetCoordinates)
  } yield tpCoords(targetCoordinates, targetDirection, tpOffset)

  object UseCheck {
    private def cellToTeleportLooksFreeToStand(a: Ability, targetId: CharacterId, tpOffset: Int)(implicit
        gameState: GameState
    ): Boolean =
      tpCoordsOpt(a, targetId, tpOffset)
        .flatMap(_.toCellOpt)
        .exists(_.looksFreeToStand(a.parentCharacter.id))

    def CellToTeleportLooksFreeToStand(a: Ability, targetId: CharacterId, tpOffset: Int)(implicit
        gameState: GameState
    ): UseCheck =
      cellToTeleportLooksFreeToStand(
        a,
        targetId,
        tpOffset,
      ) -> "Cell to teleport is not free to stand or does not exist."
  }
}
