package com.tosware.NKM.models
import enumeratum._

sealed trait CommandResponse extends EnumEntry
object CommandResponse extends Enum[CommandResponse] {
  val values = findValues

  case object Success extends CommandResponse
  case object Failure extends CommandResponse
}


