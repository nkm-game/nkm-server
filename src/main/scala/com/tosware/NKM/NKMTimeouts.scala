package com.tosware.NKM

import akka.util.Timeout

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

trait NKMTimeouts {
  private val _atMost = 3000.millis
  implicit val atMost: Duration = _atMost
  implicit val timeout: Timeout = Timeout(_atMost)

  def aw[A](f: Future[A]): A = Await.result(f, atMost)
}
