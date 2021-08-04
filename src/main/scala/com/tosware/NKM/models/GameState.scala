package com.tosware.NKM.models

case class GameState(hexMap: Option[HexMap],
                     characterIdsOutsideMap: List[String],
                     phase: Phase,
                     turn: Turn,
                     players: List[Player],
                     isStarted: Boolean,
                    )
object GameState {
  def empty: GameState = GameState(None, List(), Phase(0), Turn(0), List(), isStarted = false)
}
