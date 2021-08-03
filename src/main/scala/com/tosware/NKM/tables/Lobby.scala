package com.tosware.NKM.tables
import com.tosware.NKM.models.lobby.LobbyState
import slick.jdbc.MySQLProfile.api._
import slick.lifted.Tag
import spray.json._

import java.time.LocalDate

class Lobby(tag: Tag) extends Table[LobbyState](tag, "lobby")
with DefaultJsonProtocol
{
  def id = column[String]("ID", O.PrimaryKey)
  def name = column[Option[String]]("NAME")
  def hostUserID = column[Option[String]]("HOST_USER_ID")
  def creationDate = column[Option[LocalDate]]("CREATION_DATE")
  def chosenHexMapName = column[Option[String]]("HEX_MAP_NAME")
  def userIds = column[String]("USER_IDS")
  override def * = (id, name, hostUserID, creationDate, chosenHexMapName, userIds).shaped <>
    (
      {
        case (id, name, hostUserID, creationDate, chosenHexMapName, userIds) =>
          LobbyState(id, name, hostUserID, creationDate, chosenHexMapName, userIds.parseJson.convertTo[List[String]])
      },
      { l: LobbyState =>
        Some((l.id, l.name, l.hostUserId, l.creationDate, l.chosenHexMapName, l.userIds.toJson.toString))
      }
    )
}
