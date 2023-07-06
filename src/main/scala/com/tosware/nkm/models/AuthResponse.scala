package com.tosware.nkm.models

case class AuthResponse(
  token: String,
  userState: UserStateView
)
