package com.tosware.NKM.models.game

case class GameState(id: String,
                     hexMap: Option[HexMap],
                     characterIdsOutsideMap: Seq[String],
                     phase: Phase,
                     turn: Turn,
                     players: Seq[Player],
                     gamePhase: GamePhase,
                     pickType: PickType,
                     numberOfBans: Int,
                     numberOfCharactersPerPlayers: Int,
                    ) {
  def getCurrentPlayerNumber: Int = turn.number % players.length
  def getCurrentPlayer: Player = players(getCurrentPlayerNumber)
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
