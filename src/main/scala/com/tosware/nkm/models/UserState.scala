package com.tosware.nkm.models

import com.tosware.nkm._

case class UserState(
  email: String,
  userId: Option[UserId] = None,
  passwordHash: Option[String] = None,
  registered: Boolean = false,
)
