package com.tosware.NKM.providers

import com.tosware.NKM.models.game.hex.HexMap
import com.tosware.NKM.serializers.NKMJsonProtocol
import spray.json._

import java.io.File
import java.util.jar.JarFile
import scala.io.Source
import scala.jdk.CollectionConverters.EnumerationHasAsScala

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

}
