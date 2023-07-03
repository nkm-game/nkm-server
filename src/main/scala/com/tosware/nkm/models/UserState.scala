package com.tosware.nkm.models

import com.tosware.nkm.*

case class UserState(
  email: String,
  userId: Option[UserId] = None,
  passwordHash: Option[String] = None,
  isRegistered: Boolean = false,
  isAdmin: Boolean = false,
)
