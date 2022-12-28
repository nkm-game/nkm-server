package com.tosware.nkm.models.game.hex.testmap

import com.tosware.nkm.models.game.hex.HexCellType._
import com.tosware.nkm.models.game.hex._

object FiberDecapication {
  def hexMap: TestHexMap = TestHexMap(
    TestHexMapName.FiberDecapication,
    Set(
      (0, 0, SpawnPoint, 0),
      (1, 0),
      (2, 0),
      (3, 0, SpawnPoint, 1),
      (4, 0),
      (5, 0),
      (6, 0),
      (7, 0, Wall),
    ),
  )
}
