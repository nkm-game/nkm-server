package com.tosware.NKM

import akka.util.Timeout
import scala.concurrent.duration._

trait NKMTimeouts {
  implicit val atMost: Duration = 2000.millis
  implicit val timeout: Timeout = Timeout(2000.millis)
}
