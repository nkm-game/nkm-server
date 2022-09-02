package com.tosware.nkm.models.game

import com.tosware.nkm.NkmConf

object ClockConfig {
  def empty(): ClockConfig =
    ClockConfig(0, 0, 0, 0, 0)
  def defaultForPickType(pickType: PickType): ClockConfig = pickType match {
    case PickType.AllRandom => fromNkmConf("clock.allRandom")
    case PickType.DraftPick => fromNkmConf("clock.draftPick")
    case PickType.BlindPick => fromNkmConf("clock.blindPick")
  }
  def fromNkmConf(path: String): ClockConfig =
    ClockConfig(
      initialTimeMillis = NkmConf.int(s"$path.initialTimeMillis"),
      incrementMillis = NkmConf.int(s"$path.incrementMillis"),
      maxBanTimeMillis = NkmConf.int(s"$path.maxBanTimeMillis"),
      maxPickTimeMillis = NkmConf.int(s"$path.maxPickTimeMillis"),
      timeAfterPickMillis = NkmConf.int(s"$path.timeAfterPickMillis"),
    )
}

case class ClockConfig(
                        initialTimeMillis: Long,
                        incrementMillis: Long,
                        maxBanTimeMillis: Long,
                        maxPickTimeMillis: Long,
                        timeAfterPickMillis: Long,
                      )