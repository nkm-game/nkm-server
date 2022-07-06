package com.tosware.NKM.models.game

object HexMap {
  def empty(): HexMap = HexMap("Empty HexMap", Set.empty)
}

case class HexMap(name: String, cells: Set[HexCell]) {
  def getCell(hexCoordinates: HexCoordinates): Option[HexCell] = cells.find(_.coordinates == hexCoordinates)

  def getSpawnPoints: Set[HexCell] = cells.filter(c => c.cellType == HexCellType.SpawnPoint)

  def getSpawnPointsByNumber(n: Int): Set[HexCell] = getSpawnPoints.filter(_.spawnNumber.forall(_ == n))

  def maxNumberOfCharacters: Int = getSpawnPoints.map(_.spawnNumber.get).toSet.size

  override def toString: String = name
}
