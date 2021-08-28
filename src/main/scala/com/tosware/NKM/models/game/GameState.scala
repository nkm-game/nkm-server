package com.tosware.NKM.models.game

case class GameState(id: String,
                     hexMap: Option[HexMap],
                     characterIdsOutsideMap: List[String],
                     phase: Phase,
                     turn: Turn,
                     players: List[Player],
                     gamePhase: GamePhase,
                     pickType: PickType,
                     numberOfBans: Int,
                     numberOfCharactersPerPlayers: Int,
                    ) {
  def getCurrentPlayer = players(turn.number % players.length)
}
object GameState {
  def empty(id: String): GameState = GameState(
    id = id,
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
