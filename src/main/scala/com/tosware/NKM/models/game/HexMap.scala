package com.tosware.NKM.models.game

case class HexMap(name: String, cells: List[HexCell]) {
  def getCell(hexCoordinates: HexCoordinates) = cells.find(_.coordinates == hexCoordinates)
  def getSpawnPoints = cells.filter(c => c.cellType == HexCellType.SpawnPoint)
  def getSpawnPointsByNumber(n: Int) = getSpawnPoints.filter(_.spawnNumber.forall(_ == n))
  def maxNumberOfCharacters = getSpawnPoints.map(_.spawnNumber.get).toSet.size
}
