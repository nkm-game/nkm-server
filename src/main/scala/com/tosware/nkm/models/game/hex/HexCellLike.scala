package com.tosware.nkm.models.game.hex

import com.tosware.nkm.*
import com.tosware.nkm.models.game.character.NkmCharacter
import com.tosware.nkm.models.game.game_state.GameState

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
    characterOpt.exists(_.isFriendForC(forCharacterId))
  def isFreeToPass(forCharacterId: CharacterId)(implicit gameState: GameState): Boolean =
    isFreeToStand || isFriendStanding(forCharacterId) || gameState.characterById(forCharacterId).isFlying
  def characterOpt(implicit gameState: GameState): Option[NkmCharacter] =
    characterId.map(cid => gameState.characterById(cid))
  def looksEmpty(forCharacterId: CharacterId)(implicit gameState: GameState): Boolean =
    characterOpt match {
      case Some(character) if character.isEnemyForC(forCharacterId) => character.isInvisible
      case Some(friendlyCharacter)                                  => false
      case None                                                     => true
    }
  def looksFreeToStand(forCharacterId: CharacterId)(implicit gameState: GameState): Boolean =
    looksEmpty(forCharacterId) && !isWall
  def looksFreeToPass(forCharacterId: CharacterId)(implicit gameState: GameState): Boolean =
    looksFreeToStand(forCharacterId)
      || isFriendStanding(forCharacterId)
      || gameState.characterById(forCharacterId).isFlying

}
