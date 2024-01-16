package com.tosware.nkm

import org.slf4j.{Logger, LoggerFactory, MDC}

trait Logging {
  val logger: Logger = LoggerFactory.getLogger(getClass)
}

object Logging {
  def withGameContext[A](gameId: String, logCategory: String)(block: => A): A = {
    MDC.put("gameId", gameId)
    MDC.put("gameType", logCategory)
    try block
    finally {
      MDC.remove("gameId")
      MDC.remove("gameType")
    }
  }
}
