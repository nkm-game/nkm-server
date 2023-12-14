package com.tosware.nkm.models

object CommandResponse {
  sealed trait CommandResponse {
    val toBoolean: Boolean = this match {
      case Success(_) => true
      case Failure(_) => false
    }
  }

  case class Success(msg: String = "") extends CommandResponse

  case class Failure(msg: String = "") extends CommandResponse
}
