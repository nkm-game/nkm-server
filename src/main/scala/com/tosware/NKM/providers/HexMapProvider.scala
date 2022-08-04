package com.tosware.NKM.providers

import com.tosware.NKM.models.game.AbilityType.Normal
import com.tosware.NKM.models.game.AttackType.findValues
import com.tosware.NKM.models.game.hex.HexCellType.{SpawnPoint, Wall}
import com.tosware.NKM.models.game.hex.{HexCell, HexMap, HexUtils}
import com.tosware.NKM.providers.HexMapProvider.TestHexMapName
import com.tosware.NKM.serializers.NKMJsonProtocol
import enumeratum.{Enum, EnumEntry}
import spray.json._

import java.io.File
import java.util.jar.JarFile
import scala.io.Source
import scala.jdk.CollectionConverters.EnumerationHasAsScala

object HexMapProvider {
  sealed trait TestHexMapName extends EnumEntry
  object TestHexMapName extends Enum[TestHexMapName] {
    val values = findValues

    case object Simple2v2 extends TestHexMapName
  }

}

case class HexMapProvider() extends NKMJsonProtocol {
  def getHexMaps: Seq[HexMap] = {
    val path = "HexMaps"
    val jarFile = new File(getClass.getProtectionDomain.getCodeSource.getLocation.getPath)
    val filePaths = if (jarFile.isFile) {
      val jar = new JarFile(jarFile)
      val it = jar.entries().asScala.map(e => e.getName)
        .filter(n => n.startsWith(s"$path/") && n.endsWith(".json")).toList
      jar.close()
      it
    } else {
      val hexMapFolderPath = getClass.getResource(s"/$path/").getPath
      new File(hexMapFolderPath).listFiles
        .map(e => e.getName)
        .filter(n => n.endsWith(".json"))
        .map(n => s"$path/$n")
        .toSeq
    }
    val mapList = filePaths
      .map(p => Source.fromResource(p).mkString)
      .map(s => s.parseJson.convertTo[HexMap])

    mapList
  }

  def getTestHexMaps: Seq[HexMap] = {
    val simple1v1HexParams: Set[Any] = Set(
      (-2, 2, Wall),
      (-1, 2, Wall),
      (0, 2, Wall),
      (1, 2, Wall),
      (2, 2, Wall),
      (3, 2, Wall),
      (-1, 1),
      (0, 1),
      (1, 1),
      (2, 1),
      (3, 1),
      (-2, 0),
      (-1, 0, SpawnPoint, 0),
      (0, 0, SpawnPoint, 0),
      (1, 0),
      (2, 0),
      (3, 0, SpawnPoint, 1),
      (4, 0, SpawnPoint, 1),
      (5, 0),
      (0, -1, Wall),
      (1, -1, Wall),
      (2, -1, Wall),
      (3, -1, Wall),
      (4, -1, Wall),
    )
    Seq(
      HexMap(TestHexMapName.Simple2v2.toString, HexUtils.hexCellParamsToCells(simple1v1HexParams)),
    )
  }
}
