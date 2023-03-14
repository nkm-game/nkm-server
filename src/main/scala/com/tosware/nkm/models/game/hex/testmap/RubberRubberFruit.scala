package com.tosware.nkm.models.game.hex.testmap

import com.tosware.nkm.models.game.hex.HexCellType._
import com.tosware.nkm.models.game.hex._

object RubberRubberFruit {
  def hexMap: TestHexMap = TestHexMap(
    TestHexMapName.RubberRubberFruit,
    Set(
      (-6, 0, Wall),
      (-5, 0),
      (-4, 0, Wall),
      (-3, 0),
      (-2, 0, Wall),
      (-1, 0),
      (0, 0, SpawnPoint, 0),
      (1, 0, SpawnPoint, 1),
      (2, 0),
      (3, 0),
      (4, 0),
      (5, 0),
      (6, 0),
      (7, 0),
      (8, 0, SpawnPoint, 1),
      (9, 0),
      (10, 0),
      (11, 0, Wall),
      (12, 0),
      (13, 0),
    ),
  )
}
