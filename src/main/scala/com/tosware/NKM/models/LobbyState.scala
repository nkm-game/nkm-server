package com.tosware.NKM.models

import java.time.LocalDate

case class LobbyState(
                      id: String,
                      name: Option[String] = None,
                      hostUserId: Option[String] = None,
                      creationDate: Option[LocalDate] = None,
                      userIds: List[String] = List.empty,
                     )
{
  def created(): Boolean = creationDate.isDefined
}
