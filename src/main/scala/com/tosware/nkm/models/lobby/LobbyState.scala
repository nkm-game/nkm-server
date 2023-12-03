package com.tosware.nkm.models.lobby

import com.tosware.nkm.UserId
import com.tosware.nkm.models.NkmColor
import com.tosware.nkm.models.game.ClockConfig
import com.tosware.nkm.models.game.pick.PickType
import com.tosware.nkm.models.game.pick.PickType.AllRandom

import java.time.ZonedDateTime

case class LobbyState(
    id: String,
    name: Option[String] = None,
    hostUserId: Option[UserId] = None,
    creationDate: Option[ZonedDateTime] = None,
    chosenHexMapName: Option[String] = None,
    userIds: Seq[UserId] = Seq.empty,
    pickType: PickType = AllRandom,
    numberOfCharactersPerPlayer: Int = 1,
    numberOfBans: Int = 0,
    clockConfig: ClockConfig = ClockConfig.empty(),
    playerColors: Map[UserId, NkmColor] = Map.empty,
    gameStarted: Boolean = false,
) {
  def created(): Boolean = creationDate.isDefined
}
