package com.tosware.nkm.models.game

import com.tosware.nkm.models.game.Player.PlayerId

object Clock {
  def fromConfig(config: ClockConfig, playerOrder: Seq[PlayerId]): Clock =
    Clock(playerOrder.map(p => p -> config.initialTimeMillis).toMap)

  def empty(playerOrder: Seq[PlayerId]): Clock =
    fromConfig(ClockConfig.empty(), playerOrder)
}

case class Clock(
                  playerTimes: Map[PlayerId, Long],
                  pickTime: Long = 0,
                  isRunning: Boolean = true,
                ) {
  def setPickTime(timeMillis: Long): Clock =
    copy(pickTime = timeMillis)

  def setTime(playerId: PlayerId, timeMillis: Long): Clock =
    copy(playerTimes = playerTimes.updated(playerId, timeMillis))

  def decreasePickTime(timeMillis: Long): Clock =
    copy(pickTime = Math.max(pickTime - timeMillis, 0))

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

