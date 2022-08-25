package com.tosware.nkm

import com.typesafe.config._

object NkmConf {
  val config: Config = ConfigFactory.load("nkm.conf")
  def int(path: String): Int = config.getInt(path)
  def string(path: String): String = config.getString(path)
}
