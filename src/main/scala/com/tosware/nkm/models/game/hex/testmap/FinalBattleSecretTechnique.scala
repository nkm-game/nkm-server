package com.tosware.nkm.models.game.hex.testmap

import com.tosware.nkm.models.game.hex.HexCellType._
import com.tosware.nkm.models.game.hex._

object FinalBattleSecretTechnique {
  def hexMap: TestHexMap = TestHexMap(
    TestHexMapName.FinalBattleSecretTechnique,
    Set(
      (0, 0, SpawnPoint, 0),
      (1, 0),
      (2, 0),
      (3, 0),
      (4, 0),
      (5, 0, SpawnPoint, 1),
      (6, 0),
      (7, 0),
      (8, 0, SpawnPoint, 1),
      (1, 1),
      (2, 1),
      (3, 1),
      (4, 1),
      (5, 1),
      (6, 1, SpawnPoint, 1),
      (7, 1, SpawnPoint, 1),
    ),
  )
}
