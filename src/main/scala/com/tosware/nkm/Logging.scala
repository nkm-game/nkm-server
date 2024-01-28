package com.tosware.nkm

import org.slf4j.*

trait Logging {
  val logger: Logger = LoggerFactory.getLogger(getClass)
  val log: Logger = logger
}

object Logging {
  private def compositeKey = "compositeKey"

  private def withMdcContext[A](mdcKey: String, mdcValue: String)(block: => A): A = {
    MDC.put(mdcKey, mdcValue)
    try block
    finally
      MDC.remove(mdcKey)
  }

  private def withContext[A](logCategory: LogCategory = LogCategory.Unknown, gameId: GameId)(block: => A): A =
    withMdcContext(compositeKey, s"${logCategory.entryName.toLowerCase}/$gameId")(block)

  def withGameContext[A](gameId: GameId)(block: => A): A =
    withContext(LogCategory.Game, gameId)(block)

  def withGameRecoveryContext[A](gameId: GameId)(block: => A): A =
    withContext(LogCategory.GameRecovery, gameId)(block)

  def withLobbyContext[A](gameId: GameId)(block: => A): A =
    withContext(LogCategory.Lobby, gameId)(block)

  def withLobbyRecoveryContext[A](gameId: GameId)(block: => A): A =
    withContext(LogCategory.LobbyRecovery, gameId)(block)

  def withInternalContext[A](block: => A): A =
    withMdcContext(compositeKey, LogCategory.Internal.entryName.toLowerCase)(block)

}

import enumeratum.{Enum, EnumEntry}

sealed trait LogCategory extends EnumEntry
object LogCategory extends Enum[LogCategory] {
  val values = findValues

  case object Game extends LogCategory
  case object GameRecovery extends LogCategory
  case object Lobby extends LogCategory
  case object LobbyRecovery extends LogCategory
  case object Internal extends LogCategory
  case object Unknown extends LogCategory
}
