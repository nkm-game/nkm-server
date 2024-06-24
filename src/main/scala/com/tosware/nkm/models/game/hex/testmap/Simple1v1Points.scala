package com.tosware.nkm.models.game.hex.testmap

import com.tosware.nkm.models.game.hex.*
import com.tosware.nkm.models.game.hex.HexCellType.*

object Simple1v1Points {
  def hexMap: TestHexMap = TestHexMap(
    TestHexMapName.Simple1v1,
    Set(
      (-1, 0),
      (0, 0, SpawnPoint, 0),
      (1, 0, SpawnPoint, 1),
      (2, 0),
    ),
    Seq(
      Set((-1, 0)),
      Set((2, 0)),
    ),
  )
}
