package com.tosware.NKM.models.game.hex

import com.tosware.NKM.models.game.GameState
import com.tosware.NKM.models.game.NKMCharacter.CharacterId
import com.tosware.NKM.models.game.hex.HexCellType.Normal

case class HexCellEffect(cooldown: Int)

object HexCell {
  def empty
  (
    coordinates: HexCoordinates,
    cellType: HexCellType = Normal,
    spawnNumber: Option[Int] = None,
  ): HexCell = HexCell(coordinates, cellType, None, Seq.empty, spawnNumber)
}

case class HexCell
(
  coordinates: HexCoordinates,
  cellType: HexCellType,
  characterId: Option[CharacterId],
  effects: Seq[HexCellEffect],
  spawnNumber: Option[Int],
) {
  def isEmpty: Boolean = characterId.isEmpty
  def isFreeToStand: Boolean = isEmpty && cellType != HexCellType.Wall
  def isFreeToMove(forCharacterId: CharacterId)(implicit gameState: GameState): Boolean =
    cellType != HexCellType.Wall && characterId.flatMap(cid => gameState.characterById(cid)).fold(true)(_.isFriendFor(forCharacterId))
}
