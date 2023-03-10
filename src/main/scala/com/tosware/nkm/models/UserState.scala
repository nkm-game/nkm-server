package com.tosware.nkm.models

import com.tosware.nkm._

case class UserState(login: UserId, email: Option[String] = None, passwordHash: Option[String] = None) {
  def registered(): Boolean =
    passwordHash.isDefined
}
