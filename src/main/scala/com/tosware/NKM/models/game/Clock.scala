package com.tosware.NKM.models.game

import com.tosware.NKM.models.game.Player.PlayerId

object ClockConfig {
  def empty(): ClockConfig =
    ClockConfig(0, 0, 0, 0, 0, 0)
  def defaultForPickType(pickType: PickType) = pickType match {
    case PickType.AllRandom => ClockConfig(5000, 30000, 0, 0, 0, 30000)
    case PickType.DraftPick => ClockConfig(5000, 30000, 0, 45000, 30000, 30000)
    case PickType.BlindPick => ClockConfig(5000, 30000, 0, 0, 30000, 10000)
  }
}

case class ClockConfig(
                        initialTimeMillis: Long,
                        timePerMoveMillis: Long,
                        incrementMillis: Long,
                        maxBanTimeMillis: Long,
                        maxPickTimeMillis: Long,
                        timeAfterPickMillis: Long,
                      ) {
}

object Clock {
  def fromConfig(config: ClockConfig, playerOrder: Seq[PlayerId]): Clock =
    Clock(config, playerOrder.map(p => p -> config.initialTimeMillis).toMap)

  def empty(playerOrder: Seq[PlayerId]): Clock =
    fromConfig(ClockConfig.empty(), playerOrder)

}

case class Clock(
                  config: ClockConfig,
                  playerTimes: Map[PlayerId, Long],
                  isRunning: Boolean = true,
                ) {
  def decreaseTime(playerId: PlayerId, timeMillis: Long): Clock = {
    val newTime = Math.max(playerTimes(playerId) - timeMillis, 0)
    copy(playerTimes = playerTimes.updated(playerId, newTime))
  }

  def increaseTime(playerId: PlayerId, timeMillis: Long): Clock = {
    val newTime = playerTimes(playerId) + timeMillis
    copy(playerTimes = playerTimes.updated(playerId, newTime))
  }

  def pause(): Clock = {
    copy(isRunning = false)
  }
  def unpause(): Clock = {
    copy(isRunning = true)
  }
}
