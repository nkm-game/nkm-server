package com.tosware.NKM

import com.typesafe.config._

object NKMConf {
  val config: Config = ConfigFactory.load("nkm.conf")
  def int(path: String): Int = config.getInt(path)
  def string(path: String): String = config.getString(path)
}
