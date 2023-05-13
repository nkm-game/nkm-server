package com.tosware.nkm.models.game.hex.testmap

import com.tosware.nkm.models.game.hex.HexCellType.*
import com.tosware.nkm.models.game.hex.*

object Simple1v1 {
  def hexMap: TestHexMap = TestHexMap(
    TestHexMapName.Simple1v1,
    Set(
      (0, 0, SpawnPoint, 0),
      (1, 0, SpawnPoint, 1),
    ),
  )
}
