package com.tosware.NKM.models.game

case class NKMCharacterMetadata
(
  name: String,
  initialHealthPoints: Int,
  initialAttackPoints: Int,
  initialBasicAttackRange: Int,
  initialSpeed: Int,
  initialPsychicalDefense: Int,
  initialMagicalDefense: Int,
  initialAbilitiesMetadataIds: List[String],
) {
  val id = name
}

case class NKMCharacterState
(
 name: String,
 healthPoints: Int,
 attackPoints: Int,
 basicAttackRange: Int,
 speed: Int,
 psychicalDefense: Int,
 magicalDefense: Int,
)
case class NKMCharacter
(
  id: String,
  metadataId: String,
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
