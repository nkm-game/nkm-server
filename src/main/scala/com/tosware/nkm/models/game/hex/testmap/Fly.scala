package com.tosware.nkm.models.game.hex.testmap

import com.tosware.nkm.models.game.hex.HexCellType._
import com.tosware.nkm.models.game.hex._

object Fly {
  def hexMap: TestHexMap = TestHexMap(
    TestHexMapName.Fly,
    Set(
      (0, 0, SpawnPoint, 0),
      (1, 0, Wall),
      (2, 0),
      (3, 0, SpawnPoint, 1),
      (4, 0),
    ),
  )
}
