package com.tosware.NKM

import akka.util.Timeout
import scala.concurrent.duration._

trait NKMTimeouts {
  implicit val atMost: Duration = 1000.millis
  implicit val timeout: Timeout = Timeout(1000.millis)
}
