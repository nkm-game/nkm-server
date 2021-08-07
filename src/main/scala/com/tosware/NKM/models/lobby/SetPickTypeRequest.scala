package com.tosware.NKM.models.lobby

import com.tosware.NKM.models.game.PickType

case class SetPickTypeRequest(lobbyId: String, pickType: PickType)
