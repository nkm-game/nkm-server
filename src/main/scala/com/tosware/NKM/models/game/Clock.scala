package com.tosware.NKM.models.game

import com.tosware.NKM.models.game.Player.PlayerId

object ClockConfig {
  def empty(): ClockConfig =
    ClockConfig(Seq(), 0, 0, 0, 0, 0, 0)
  def draftPickConfig(playerOrder: Seq[PlayerId]) = ClockConfig(playerOrder, 0, 30000, 0, 45000, 30000, 30000)

}

case class ClockConfig(
                        playerOrder: Seq[PlayerId],
                        initialTimeMillis: Long,
                        timePerMoveMillis: Long,
                        incrementMillis: Long,
                        maxBanTimeMillis: Long,
                        maxPickTimeMillis: Long,
                        timeAfterPickMillis: Long,
                      ) {
  def initialPlayerTimes: Map[PlayerId, Long] =
    playerOrder.map(p => p -> initialTimeMillis).toMap

}

object Clock {
  def empty(): Clock = {
    def config = ClockConfig.empty()
    Clock(config, config.initialPlayerTimes)
  }

  def fromConfig(config: ClockConfig): Clock =
    Clock(config, config.initialPlayerTimes)

}

case class Clock(
                  config: ClockConfig,
                  playerTimes: Map[PlayerId, Long],
                ) {
  def decreaseTime(playerId: PlayerId, timeMillis: Long): Clock = {
    val newTime = Math.max(playerTimes(playerId) - timeMillis, 0)
    copy(playerTimes = playerTimes.updated(playerId, newTime))
  }

  def increaseTime(playerId: PlayerId, timeMillis: Long): Clock = {
    val newTime = playerTimes(playerId) + timeMillis
    copy(playerTimes = playerTimes.updated(playerId, newTime))
  }
}
