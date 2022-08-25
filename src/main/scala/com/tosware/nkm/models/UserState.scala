package com.tosware.nkm.models

import UserState._

object UserState {
  type UserId = String
}

case class UserState(login: UserId, email: Option[String] = None, passwordHash: Option[String] = None) {
  def registered(): Boolean =
    passwordHash.isDefined
}
