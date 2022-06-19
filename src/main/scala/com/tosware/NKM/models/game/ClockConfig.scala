package com.tosware.NKM.models.game


object ClockConfig {
  def empty(): ClockConfig =
    ClockConfig(0, 0, 0, 0, 0)
  def defaultForPickType(pickType: PickType): ClockConfig = pickType match {
    case PickType.AllRandom => ClockConfig(5 * 60 * 1000, 30 * 1000, 0, 0, 30 * 1000)
    case PickType.DraftPick => ClockConfig(5 * 60 * 1000, 30 * 1000, 45 * 1000, 30 * 1000, 30 * 1000)
    case PickType.BlindPick => ClockConfig(5 * 60 * 1000, 30 * 1000, 0, 30 * 1000, 10 * 1000)
  }
}

case class ClockConfig(
                        initialTimeMillis: Long,
                        incrementMillis: Long,
                        maxBanTimeMillis: Long,
                        maxPickTimeMillis: Long,
                        timeAfterPickMillis: Long,
                      )

