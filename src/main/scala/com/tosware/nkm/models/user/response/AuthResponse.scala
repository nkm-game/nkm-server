package com.tosware.nkm.models.user.response

import com.tosware.nkm.models.user.UserStateView

case class AuthResponse(
    token: String,
    userState: UserStateView,
)
