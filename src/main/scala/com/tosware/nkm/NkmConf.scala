package com.tosware.nkm

import com.typesafe.config.*

import scala.jdk.CollectionConverters.*

object NkmConf extends Logging {
  object AutoExtract {
    case class Path(value: String)
  }
  trait AutoExtract {
    import AutoExtract.*

    implicit val path: Path = Path(
      this.getClass.getCanonicalName
        .stripPrefix("com.tosware.nkm.models.game.")
        .stripSuffix("$")
    )
  }


  val config: Config = ConfigFactory.load("nkm.conf")
  def int(path: String): Int = config.getInt(path)
  def string(path: String): String = config.getString(path)
  def extract(path: String): Map[String, Int] = {
    try {
      config.getObject(path).unwrapped().asScala.view.mapValues(_.toString.toInt).toMap
    } catch {
      case e: ConfigException if e.getClass.getSimpleName == "Missing" =>
        logger.warn(s"Config missing at path [$path]")
        Map.empty
      case e: ConfigException =>
        e.printStackTrace()
        Map.empty
    }
  }
}
