package com.tosware.nkm.models

import com.tosware.nkm.*

case class UserState(
  email: String,
  userId: Option[UserId] = None,
  passwordHashOpt: Option[String] = None,
  isRegistered: Boolean = false,
  isAdmin: Boolean = false,
) {
  def toView: UserStateView =
    UserStateView(email, userId, isAdmin)
}
