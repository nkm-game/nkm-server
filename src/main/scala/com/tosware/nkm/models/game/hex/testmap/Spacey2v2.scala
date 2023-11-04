package com.tosware.nkm.models.game.hex.testmap

import com.tosware.nkm.models.game.hex.*
import com.tosware.nkm.models.game.hex.HexCellType.*

object Spacey2v2 {
  def hexMap: TestHexMap = TestHexMap(
    TestHexMapName.Spacey2v2,
    Set(
      (-7, 0),
      (-6, 0),
      (-5, 0),
      (-4, 0),
      (-3, 0),
      (-2, 0),
      (-1, 0, SpawnPoint, 0),
      (0, 0, SpawnPoint, 0),
      (1, 0, SpawnPoint, 1),
      (2, 0, SpawnPoint, 1),
      (3, 0),
      (4, 0),
      (5, 0),
      (6, 0),
      (7, 0),
    ),
  )
}
