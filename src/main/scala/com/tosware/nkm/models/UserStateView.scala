package com.tosware.nkm.models

import com.tosware.nkm.*

case class UserStateView(
    email: String,
    userId: Option[UserId],
    isAdmin: Boolean,
)
