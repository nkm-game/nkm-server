package com.tosware.nkm.models.game.hex
import enumeratum.{Enum, EnumEntry}

sealed trait TestHexMapName extends EnumEntry
object TestHexMapName extends Enum[TestHexMapName] {
  val values = findValues

  case object FiberDecapitation extends TestHexMapName
  case object FinalBattleSecretTechnique extends TestHexMapName
  case object Fly extends TestHexMapName
  case object OgreCutter extends TestHexMapName
  case object RubberRubberFruit extends TestHexMapName
  case object Simple1v1 extends TestHexMapName
  case object Simple1v9Line extends TestHexMapName
  case object Simple2v2 extends TestHexMapName
  case object Simple2v2Points extends TestHexMapName
  case object Simple2v2Wall extends TestHexMapName
  case object Simple2v2v2 extends TestHexMapName
  case object Spacey1v1 extends TestHexMapName
  case object Spacey2v2 extends TestHexMapName
  case object SummerBreeze extends TestHexMapName
}
