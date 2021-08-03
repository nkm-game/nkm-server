package com.tosware.NKM.models.lobby

import java.time.LocalDate

case class LobbyState(
                      id: String,
                      name: Option[String] = None,
                      hostUserId: Option[String] = None,
                      creationDate: Option[LocalDate] = None,
                      chosenHexMapName: Option[String] = None,
                      userIds: List[String] = List.empty,
                     )
{
  def created(): Boolean = creationDate.isDefined
}
