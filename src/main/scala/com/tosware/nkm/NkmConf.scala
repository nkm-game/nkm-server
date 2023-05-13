package com.tosware.nkm

import com.typesafe.config.*

import scala.jdk.CollectionConverters.*

object NkmConf extends Logging {
  val config: Config = ConfigFactory.load("nkm.conf")
  def int(path: String): Int = config.getInt(path)
  def string(path: String): String = config.getString(path)
  def extract(path: String): Map[String, Int] = {
    try {
      config.getObject(path).unwrapped().asScala.view.mapValues(_.toString.toInt).toMap
    } catch {
      case e: ConfigException =>
        e.printStackTrace()
        Map.empty
    }
  }
}
