package com.tosware.nkm.models.game.hex

import com.tosware.nkm.*
import com.tosware.nkm.models.game.GameState
import com.tosware.nkm.models.game.character.NkmCharacter

trait HexCellLike {
  val coordinates: HexCoordinates
  val cellType: HexCellType
  val characterId: Option[CharacterId]
  val spawnNumber: Option[Int]

  def isEmpty: Boolean =
    characterId.isEmpty
  def isWall: Boolean =
    cellType == HexCellType.Wall
  def isFreeToStand: Boolean =
    isEmpty && !isWall
  def isFriendStanding(forCharacterId: CharacterId)(implicit gameState: GameState): Boolean =
    !isEmpty && gameState.characterById(forCharacterId).isFriendForC(characterId.get)
  def isFreeToPass(forCharacterId: CharacterId)(implicit gameState: GameState): Boolean =
    isFreeToStand || isFriendStanding(forCharacterId) || gameState.characterById(forCharacterId).isFlying
  def characterOpt(implicit gameState: GameState): Option[NkmCharacter] =
    characterId.map(cid => gameState.characterById(cid))

  def looksEmpty(forCharacterId: CharacterId)(implicit gameState: GameState): Boolean =
    isEmpty || {
      val character = gameState.characterById(characterId.get)
      character.isInvisible && character.isEnemyForC(forCharacterId)
    }
  def looksFreeToStand(forCharacterId: CharacterId)(implicit gameState: GameState): Boolean =
    looksEmpty(forCharacterId) && !isWall
  def looksFreeToPass(forCharacterId: CharacterId)(implicit gameState: GameState): Boolean =
    looksFreeToStand(forCharacterId)
      || isFriendStanding(forCharacterId)
      || gameState.characterById(forCharacterId).isFlying

}
