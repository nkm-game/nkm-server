package com.tosware.nkm

import scala.jdk.CollectionConverters._
import com.typesafe.config._

object NkmConf {
  val config: Config = ConfigFactory.load("nkm.conf")
  def int(path: String): Int = config.getInt(path)
  def string(path: String): String = config.getString(path)
  def extract(path: String): Map[String, Int] = config.getObject(path).unwrapped().asScala.view.mapValues(_.toString.toInt).toMap
}