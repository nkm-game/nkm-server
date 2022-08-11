package com.tosware.NKM.models.game

import com.tosware.NKM.NKMConf


object ClockConfig {
  def empty(): ClockConfig =
    ClockConfig(0, 0, 0, 0, 0)
  def defaultForPickType(pickType: PickType): ClockConfig = pickType match {
    case PickType.AllRandom => fromNKMConf("clock.allRandom")
    case PickType.DraftPick => fromNKMConf("clock.draftPick")
    case PickType.BlindPick => fromNKMConf("clock.blindPick")
  }
  def fromNKMConf(path: String): ClockConfig =
    ClockConfig(
      initialTimeMillis = NKMConf.int(s"$path.initialTimeMillis"),
      incrementMillis = NKMConf.int(s"$path.incrementMillis"),
      maxBanTimeMillis = NKMConf.int(s"$path.maxBanTimeMillis"),
      maxPickTimeMillis = NKMConf.int(s"$path.maxPickTimeMillis"),
      timeAfterPickMillis = NKMConf.int(s"$path.timeAfterPickMillis"),
    )
}

case class ClockConfig(
                        initialTimeMillis: Long,
                        incrementMillis: Long,
                        maxBanTimeMillis: Long,
                        maxPickTimeMillis: Long,
                        timeAfterPickMillis: Long,
                      )

