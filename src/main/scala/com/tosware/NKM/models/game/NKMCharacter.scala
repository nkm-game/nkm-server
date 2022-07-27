package com.tosware.NKM.models.game

import NKMCharacter._
import NKMCharacterMetadata.CharacterMetadataId

object NKMCharacter {
  type CharacterId = String
  def fromMetadata(characterId: CharacterId, NKMCharacterMetadata: NKMCharacterMetadata) = {
    NKMCharacter(
      id = characterId,
      metadataId = NKMCharacterMetadata.id,
      state = NKMCharacterState(
        name = NKMCharacterMetadata.name,
        healthPoints = NKMCharacterMetadata.initialHealthPoints,
        attackPoints = NKMCharacterMetadata.initialAttackPoints,
        basicAttackRange = NKMCharacterMetadata.initialBasicAttackRange,
        speed = NKMCharacterMetadata.initialSpeed,
        psychicalDefense = NKMCharacterMetadata.initialPsychicalDefense,
        magicalDefense = NKMCharacterMetadata.initialMagicalDefense,
      )
    )
  }
}

case class NKMCharacter
(
  id: CharacterId,
  metadataId: CharacterMetadataId,
  state: NKMCharacterState,
)
{
  def canMove: Boolean = true

  def basicAttackCells(implicit gameState: GameState): Set[HexCell] = defaultGetBasicAttackCells

  def parentCell(implicit gameState: GameState): Option[HexCell] =
    gameState.hexMap.get.getCellOfCharacter(id)

  def defaultGetBasicAttackCells(implicit gameState: GameState): Set[HexCell] = {
    ???
  }

  def toView: NKMCharacterView = NKMCharacterView(
    id = id,
    metadataId = metadataId,
    state = state.toView,
  )
}

case class NKMCharacterView
(
  id: CharacterId,
  metadataId: CharacterMetadataId,
  state: NKMCharacterStateView,
)
