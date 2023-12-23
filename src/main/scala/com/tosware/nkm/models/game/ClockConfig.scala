package com.tosware.nkm.models.game

import com.tosware.nkm.models.CommandResponse.CommandResponse
import com.tosware.nkm.models.UseCheck
import com.tosware.nkm.models.game.pick.PickType
import com.tosware.nkm.{NkmConf, UseCheck}

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
) {
  def validate: CommandResponse = {
    val minTime = 0
    val maxTime = 1000 * 60 * 60 * 60 * 24 * 30 // 30 days

    def timeChecks(timeMillis: Long, timeName: String) = Set[UseCheck](
      (minTime <= timeMillis) -> s"$timeName is too small, needs to be at least $minTime",
      (timeMillis <= maxTime) -> s"$timeName is too big, needs to be at most $maxTime",
    )

    val useChecks = timeChecks(initialTimeMillis, "initialTimeMillis") ++
      timeChecks(incrementMillis, "incrementMillis") ++
      timeChecks(maxBanTimeMillis, "maxBanTimeMillis") ++
      timeChecks(maxPickTimeMillis, "maxPickTimeMillis") ++
      timeChecks(timeAfterPickMillis, "timeAfterPickMillis") ++
      timeChecks(timeForCharacterPlacing, "timeForCharacterPlacing")

    UseCheck.canBeUsed(useChecks)
  }
}
