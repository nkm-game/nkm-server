package com.tosware.nkm.models.game.hex

import com.tosware.nkm.models.game.GameState
import com.tosware.nkm.models.game.character.NkmCharacter
import com.tosware.nkm.models.game.character.NkmCharacter.CharacterId
import com.tosware.nkm.models.game.effects.Fly

trait HexCellLike {
  val coordinates: HexCoordinates
  val cellType: HexCellType
  val characterId: Option[CharacterId]
  val spawnNumber: Option[Int]

  def isEmpty: Boolean = characterId.isEmpty
  def isFreeToStand: Boolean = isEmpty && cellType != HexCellType.Wall
  def isFreeToPass(forCharacterId: CharacterId)(implicit gameState: GameState): Boolean = {
    val forCharacter = gameState.characterById(forCharacterId)
    cellType != HexCellType.Wall && characterId.forall(c => forCharacter.isFriendForC(c)) || forCharacter.state.effects.ofType[Fly].nonEmpty
  }

  def characterOpt(implicit gameState: GameState): Option[NkmCharacter] =
    characterId.map(cid => gameState.characterById(cid))
}
