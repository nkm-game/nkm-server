package com.tosware.nkm.models.game.hex
import enumeratum.{Enum, EnumEntry}

sealed trait TestHexMapName extends EnumEntry
object TestHexMapName extends Enum[TestHexMapName] {
  val values = findValues

  case object Simple1v1 extends TestHexMapName
  case object Simple2v2 extends TestHexMapName
  case object Simple2v2Wall extends TestHexMapName
  case object Simple2v2v2 extends TestHexMapName
  case object Simple1v9Line extends TestHexMapName
  case object OgreCutter extends TestHexMapName
  case object FiberDecapication extends TestHexMapName
  case object Fly extends TestHexMapName
}
