package com.tosware.NKM.services

import com.tosware.NKM.models.Credentials

object UserService {
  sealed trait Event
  case class LoggedIn(login: String) extends Event
  case object InvalidCredentials extends Event

  def authenticate(creds: Credentials): Event =
    if (creds.login == "tojatos" && creds.password == "password")
      LoggedIn(creds.login)
    else InvalidCredentials
  }
