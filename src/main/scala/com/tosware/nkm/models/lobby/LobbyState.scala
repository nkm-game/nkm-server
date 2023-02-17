package com.tosware.nkm.models.lobby

import com.tosware.nkm.models.game.ClockConfig
import com.tosware.nkm.models.game.pick.PickType
import com.tosware.nkm.models.game.pick.PickType.AllRandom

import java.time.LocalDateTime

case class LobbyState(
                      id: String,
                      name: Option[String] = None,
                      hostUserId: Option[String] = None,
                      creationDate: Option[LocalDateTime] = None,
                      chosenHexMapName: Option[String] = None,
                      userIds: List[String] = List.empty,
                      pickType: PickType = AllRandom,
                      numberOfCharactersPerPlayer: Int = 1,
                      numberOfBans: Int = 0,
                      clockConfig: ClockConfig = ClockConfig.empty(),
                      gameStarted: Boolean = false,
                     )
{
  def created(): Boolean = creationDate.isDefined
}
