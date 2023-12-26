package com.tosware.nkm.providers

import com.tosware.nkm.models.game.hex.*
import com.tosware.nkm.serializers.NkmJsonProtocol
import spray.json.*

import java.io.File
import java.util.jar.JarFile
import scala.io.Source
import scala.jdk.CollectionConverters.EnumerationHasAsScala

case class HexMapProvider() extends NkmJsonProtocol {
  def getHexMaps: Seq[HexMapTemplate] = {
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
      .map(s => s.parseJson.convertTo[HexMapTemplate])

    mapList
  }

  def getTestHexMaps: Seq[TestHexMap] =
    Seq(
      testmap.FiberDecapitation.hexMap,
      testmap.FinalBattleSecretTechnique.hexMap,
      testmap.Fly.hexMap,
      testmap.OgreCutter.hexMap,
      testmap.RubberRubberFruit.hexMap,
      testmap.Simple1v1.hexMap,
      testmap.Simple1v9Line.hexMap,
      testmap.Simple2v2.hexMap,
      testmap.Simple2v2Wall.hexMap,
      testmap.Simple2v2v2.hexMap,
      testmap.Spacey1v1.hexMap,
      testmap.Spacey2v2.hexMap,
      testmap.SummerBreeze.hexMap,
    )

  def getTestHexMap(name: TestHexMapName): HexMap =
    getTestHexMaps.find(_.name == name).map(_.hexMap).getOrElse(throw new Exception(
      s"Unable to find hex map with name $name"
    ))
}
