package com.tosware.nkm.models.game.hex.testmap

import com.tosware.nkm.models.game.hex.HexCellType._
import com.tosware.nkm.models.game.hex._

object Simple2v2Wall {
  def hexMap: TestHexMap = TestHexMap(
    TestHexMapName.Simple2v2Wall,
    Set(
      (-1, 0, SpawnPoint, 0),
      (0, 0, SpawnPoint, 0),
      (1, 0, Wall),
      (2, 0, Wall),
      (3, 0, SpawnPoint, 1),
      (4, 0, SpawnPoint, 1),
    ),
  )
}
