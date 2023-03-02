package com.tosware.nkm.models.game.hex

import com.tosware.nkm.models.game.Player.PlayerId
import com.tosware.nkm.models.game.character.NkmCharacter
import com.tosware.nkm.models.game.character.NkmCharacter.CharacterId
import com.tosware.nkm.models.game.effects.Fly
import com.tosware.nkm.models.game.{GameState, GameStatus}

trait HexCellLike {
  val coordinates: HexCoordinates
  val cellType: HexCellType
  val characterId: Option[CharacterId]
  val effects: Seq[HexCellEffect]
  val spawnNumber: Option[Int]

  def isEmpty: Boolean = characterId.isEmpty
  def isFreeToStand: Boolean = isEmpty && cellType != HexCellType.Wall
  def isFreeToPass(forCharacterId: CharacterId)(implicit gameState: GameState): Boolean = {
    val forCharacter = gameState.characterById(forCharacterId)
    cellType != HexCellType.Wall && characterId.forall(c => forCharacter.isFriendForC(c)) || forCharacter.state.effects.ofType[Fly].nonEmpty
  }

  def characterOpt(implicit gameState: GameState): Option[NkmCharacter] =
    characterId.map(cid => gameState.characterById(cid))

  def toView(forPlayerOpt: Option[PlayerId])(implicit gameState: GameState): HexCellView = {
    val characterIdOpt =
      if (gameState.invisibleCharacterCoords(forPlayerOpt).contains(coordinates))
        None
      else if (gameState.gameStatus == GameStatus.CharacterPlacing && characterOpt(gameState).fold(true)(c => !forPlayerOpt.contains(c.owner.id)))
        None
      else characterId

    HexCellView(
      coordinates = coordinates,
      cellType = cellType,
      characterId = characterIdOpt,
      effects = effects,
      spawnNumber = spawnNumber,
    )
  }
}
