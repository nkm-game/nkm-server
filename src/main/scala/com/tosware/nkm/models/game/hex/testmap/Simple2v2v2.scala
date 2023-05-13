package com.tosware.nkm.models.game.hex.testmap

import com.tosware.nkm.models.game.hex.HexCellType.*
import com.tosware.nkm.models.game.hex.*

object Simple2v2v2 {
  def hexMap: TestHexMap = TestHexMap(
    TestHexMapName.Simple2v2v2,
    Set(
      (-2, 2, Wall),
      (-1, 2, Wall),
      (0, 2, Wall),
      (1, 2, Wall),
      (2, 2, Wall),
      (3, 2, Wall),
      (-2, 1),
      (-1, 1),
      (0, 1),
      (1, 1, SpawnPoint, 2),
      (2, 1, SpawnPoint, 2),
      (3, 1),
      (4, 1),
      (-2, 0),
      (-1, 0, SpawnPoint, 0),
      (0, 0, SpawnPoint, 0),
      (1, 0),
      (2, 0),
      (3, 0, SpawnPoint, 1),
      (4, 0, SpawnPoint, 1),
      (5, 0),
      (0, -1, Wall),
      (1, -1, Wall),
      (2, -1, Wall),
      (3, -1, Wall),
      (4, -1, Wall),
    ),
  )
}
