package com.tosware.NKM.tables

import com.tosware.NKM.models.UserState
import slick.lifted.Tag
import slick.jdbc.MySQLProfile.api._

class User(tag: Tag) extends Table[UserState](tag, "user") {
  def login = column[String]("LOGIN", O.PrimaryKey)
  def email = column[Option[String]]("EMAIL", O.Unique)
  def passwordHash = column[Option[String]]("PASSWORD_HASH")
  override def * = (login, email, passwordHash) <> (UserState.tupled, UserState.unapply)
}
