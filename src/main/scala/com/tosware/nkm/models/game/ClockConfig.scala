package com.tosware.nkm.models.game

import com.tosware.nkm.NkmConf

object ClockConfig {
  def empty(): ClockConfig =
    ClockConfig(0, 0, 0, 0, 0, 0)
  def defaultForPickType(pickType: PickType): ClockConfig = pickType match {
    case PickType.AllRandom => fromNkmConf("clock.allRandom")
    case PickType.DraftPick => fromNkmConf("clock.draftPick")
    case PickType.BlindPick => fromNkmConf("clock.blindPick")
  }
  def fromNkmConf(path: String): ClockConfig = {
    val c = NkmConf.extract(path)
    ClockConfig(
      initialTimeMillis = c("initialTimeMillis"),
      incrementMillis = c("incrementMillis"),
      maxBanTimeMillis = c("maxBanTimeMillis"),
      maxPickTimeMillis = c("maxPickTimeMillis"),
      timeAfterPickMillis = c("timeAfterPickMillis"),
      timeForCharacterPlacing = c("timeForCharacterPlacing"),
    )
  }
}

case class ClockConfig(
  initialTimeMillis: Long,
  incrementMillis: Long,
  maxBanTimeMillis: Long,
  maxPickTimeMillis: Long,
  timeAfterPickMillis: Long,
  timeForCharacterPlacing: Long,
)
