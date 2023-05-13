package com.tosware.nkm.models.game.hex.testmap

import com.tosware.nkm.models.game.hex.HexCellType.*
import com.tosware.nkm.models.game.hex.*

object Simple1v9Line {
  def hexMap: TestHexMap = TestHexMap(
    TestHexMapName.Simple1v9Line,
    Set(
      (-1, 0, Wall),
      (0, 0, SpawnPoint, 0),
      (1, 0, SpawnPoint, 1),
      (2, 0, SpawnPoint, 1),
      (3, 0, SpawnPoint, 1),
      (4, 0, SpawnPoint, 1),
      (5, 0, SpawnPoint, 1),
      (6, 0, SpawnPoint, 1),
      (7, 0, SpawnPoint, 1),
      (8, 0, SpawnPoint, 1),
      (9, 0, SpawnPoint, 1),
      (10, 0, Wall),
    ),
  )
}
