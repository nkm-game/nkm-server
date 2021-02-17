package com.tosware.NKM.models

case class UserState(login: String, email: Option[String] = None, passwordHash: Option[String] = None) {
  def registered(): Boolean =
    passwordHash.isDefined
}
