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
        magicalDefense = NKMCharacterMetadata.initialMagicalDefense
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
  val getBasicAttackCells = (gameState: GameState) => defaultGetBasicAttackCells(gameState)

  def getParentCell(gameState: GameState): Option[HexCell] = {
    if(gameState.characterIdsOutsideMap.contains(id)) return None
    Some(gameState.hexMap.get.cells.filter(c => c.characterId.nonEmpty && c.characterId.get == id).head)
  }

  def defaultGetBasicAttackCells(gameState: GameState): List[HexCell] = {
    val parentCell = getParentCell(gameState)
    ???
  }
}
