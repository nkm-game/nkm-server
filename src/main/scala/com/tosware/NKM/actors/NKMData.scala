package com.tosware.NKM.actors

import java.io.File
import java.nio.file.{Files, Paths}

import akka.actor.{Actor, ActorLogging, Props}
import com.tosware.NKM.models.HexMap
import com.tosware.NKM.serializers.NKMJsonProtocol
import spray.json._

object NKMData {
  case object GetHexMaps

  def props(): Props = Props(new NKMData())
}

class NKMData extends Actor with ActorLogging with NKMJsonProtocol {
  import NKMData._

  override def receive: Receive = {
    case GetHexMaps =>
      val hexMapFolderPath = getClass.getResource("/HexMaps").getPath
      val mapList = new File(hexMapFolderPath).listFiles.toList
        .filter(file => file.getName.endsWith(".json"))
        .map(file => file.getPath)
        .map(path => Files.readAllBytes(Paths.get(path)))
        .map(bytes => new String(bytes))
        .map(mapString => mapString.parseJson.convertTo[HexMap])
      sender ! mapList

  }
}
