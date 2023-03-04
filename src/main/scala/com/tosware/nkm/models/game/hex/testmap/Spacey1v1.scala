package com.tosware.nkm.models.game.hex.testmap

import com.tosware.nkm.models.game.hex.HexCellType._
import com.tosware.nkm.models.game.hex._

object Spacey1v1 {
  def hexMap: TestHexMap = TestHexMap(
    TestHexMapName.Spacey1v1,
    Set(
      (-7, 0),
      (-6, 0),
      (-5, 0),
      (-4, 0),
      (-3, 0),
      (-2, 0),
      (-1, 0),
      (0, 0, SpawnPoint, 0),
      (1, 0, SpawnPoint, 1),
      (2, 0),
      (3, 0),
      (4, 0),
      (5, 0),
      (6, 0),
      (7, 0),
    ),
  )
}
