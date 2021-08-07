package com.tosware.NKM.models.game

case class GameState(hexMap: Option[HexMap],
                     characterIdsOutsideMap: List[String],
                     phase: Phase,
                     turn: Turn,
                     players: List[Player],
                     gamePhase: GamePhase,
                     pickType: PickType,
                     numberOfBans: Int,
                     numberOfCharactersPerPlayers: Int,
                    )
object GameState {
  def empty: GameState = GameState(
    hexMap = None,
    characterIdsOutsideMap = List(),
    phase = Phase(0),
    turn = Turn(0),
    players = List(),
    gamePhase = GamePhase.NotStarted,
    pickType = PickType.AllRandom,
    numberOfBans = 0,
    numberOfCharactersPerPlayers = 1,
  )
}
