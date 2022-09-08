package com.tosware.nkm.providers

import com.tosware.nkm.models.game.hex.HexCellType.{SpawnPoint, Wall}
import com.tosware.nkm.models.game.hex.{HexMap, HexUtils}
import com.tosware.nkm.providers.HexMapProvider.TestHexMapName
import com.tosware.nkm.serializers.NkmJsonProtocol
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
    case object Simple2v2Wall extends TestHexMapName
    case object Simple1v9Line extends TestHexMapName
    case object OgreCutter extends TestHexMapName
  }
}

case class HexMapProvider() extends NkmJsonProtocol {
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
    val simple2v2HexParams: Set[Any] = Set(
      (-2, 2, Wall),
      (-1, 2, Wall),
      (0, 2, Wall),
      (1, 2, Wall),
      (2, 2, Wall),
      (3, 2, Wall),
      (-2, 1),
      (-1, 1),
      (0, 1),
      (1, 1),
      (2, 1),
      (3, 1),
      (4, 1),
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
    val simple2v2WallHexParams: Set[Any] = Set(
      (-1, 0, SpawnPoint, 0),
      (0, 0, SpawnPoint, 0),
      (1, 0, Wall),
      (2, 0, Wall),
      (3, 0, SpawnPoint, 1),
      (4, 0, SpawnPoint, 1),
    )
    val ogreCutterHexParams: Set[Any] = Set(
      (0, 0, SpawnPoint, 0),
      (1, 0),
      (2, 0),
      (3, 0, SpawnPoint, 1),
      (4, 0),
      (5, 0),
      (6, 0, Wall),
    )

    val simple1v9LineHexParams: Set[Any] = Set(
      (0, 0, SpawnPoint, 0),
      (1, 0, SpawnPoint, 1),
      (2, 0, SpawnPoint, 1),
      (3, 0, SpawnPoint, 1),
      (4, 0, SpawnPoint, 1),
      (5, 0, SpawnPoint, 1),
      (6, 0, SpawnPoint, 1),
      (7, 0, SpawnPoint, 1),
      (8, 0, SpawnPoint, 1),
      (9, 0, SpawnPoint, 1),
    )
    val nameToParams: Map[TestHexMapName, Set[Any]] = Map(
      TestHexMapName.Simple2v2 -> simple2v2HexParams,
      TestHexMapName.Simple2v2Wall -> simple2v2WallHexParams,
      TestHexMapName.OgreCutter -> ogreCutterHexParams,
      TestHexMapName.Simple1v9Line -> simple1v9LineHexParams,
    )
    nameToParams.map {
      case (name: TestHexMapName, params: Set[Any]) =>
        HexMap(name.toString, HexUtils.hexCellParamsToCells(params))
    }.toSeq
  }

  def getTestHexMap(name: TestHexMapName): HexMap =
    getTestHexMaps.find(_.name == name.toString).get
}
