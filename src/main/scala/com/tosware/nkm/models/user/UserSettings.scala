package com.tosware.nkm.models.user

import com.tosware.nkm.models.NkmColor

case class UserSettings(
    language: String,
    preferredColor: Option[NkmColor],
)

object UserSettings {
  def default(): UserSettings = UserSettings("English", None)
}
