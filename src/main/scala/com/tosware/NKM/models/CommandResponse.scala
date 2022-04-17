package com.tosware.NKM.models

object CommandResponse {
  sealed trait CommandResponse

  case class Success(msg: String = "") extends CommandResponse

  case class Failure(msg: String = "") extends CommandResponse
}
