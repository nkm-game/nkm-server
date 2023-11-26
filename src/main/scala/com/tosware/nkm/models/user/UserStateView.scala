package com.tosware.nkm.models.user

import com.tosware.nkm.*

case class UserStateView(
    email: String,
    userId: Option[UserId],
    isAdmin: Boolean,
    userSettings: UserSettings,
)
