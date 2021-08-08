package com.tosware.NKM.models.lobby

import com.tosware.NKM.models.game.PickType
import com.tosware.NKM.models.game.PickType.AllRandom

import java.time.LocalDate

case class LobbyState(
                      id: String,
                      name: Option[String] = None,
                      hostUserId: Option[String] = None,
                      creationDate: Option[LocalDate] = None,
                      chosenHexMapName: Option[String] = None,
                      userIds: List[String] = List.empty,
                      pickType: PickType = AllRandom,
                      numberOfCharactersPerPlayer: Int = 1,
                      numberOfBans: Int = 0,
                     )
{
  def created(): Boolean = creationDate.isDefined
}
