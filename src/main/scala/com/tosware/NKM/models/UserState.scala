package com.tosware.NKM.models

case class UserState(login: String, passwordHash: Option[String] = None) {
  def registered(): Boolean =
    passwordHash.isDefined
}
