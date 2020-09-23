package com.tosware.NKM.actors

import java.io.File
import java.util.jar.JarFile

import akka.actor.{Actor, ActorLogging, Props}
import com.tosware.NKM.models.HexMap
import com.tosware.NKM.serializers.NKMJsonProtocol
import spray.json._

import scala.jdk.CollectionConverters._
import scala.io.Source

object NKMData {
  case object GetHexMaps
//  case class GetHexMapsResponse(hexMaps: List[HexMap])

  def props(): Props = Props(new NKMData())
}

class NKMData extends Actor with ActorLogging with NKMJsonProtocol {
  import NKMData._

  override def receive: Receive = {
    case GetHexMaps =>
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
          .toList
      }
      val mapList = filePaths
        .map(p => Source.fromResource(p).mkString)
        .map(s => s.parseJson.convertTo[HexMap])

      sender() ! mapList
  }
}
