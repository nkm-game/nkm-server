package com.tosware.nkm

import akka.util.Timeout

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

trait NkmTimeouts {
  private val _atMost = NkmConf.int("asyncTimeout").millis
  implicit val atMost: Duration = _atMost
  implicit val timeout: Timeout = Timeout(_atMost)

  def aw[A](f: Future[A]): A = Await.result(f, atMost)
}