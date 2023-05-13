package com.tosware.nkm.models.game.hex.testmap

import com.tosware.nkm.models.game.hex.HexCellType.*
import com.tosware.nkm.models.game.hex.*

object SummerBreeze {
  def hexMap: TestHexMap = TestHexMap(
    TestHexMapName.SummerBreeze,
    Set(
      (0, 0, SpawnPoint, 0),
      (1, 0),
      (2, 0),
      (3, 0, SpawnPoint, 1),
      (4, 0, SpawnPoint, 1),
      (5, 0),
      (6, 0),
      (7, 0),
      (8, 0, Wall),
    ),
  )
}
