package com.tosware.nkm.models.game.game_state.extensions

import com.softwaremill.quicklens.*
import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.character.*
import com.tosware.nkm.models.game.event.GameEvent.*
import com.tosware.nkm.models.game.game_state.{GameState, GameStatus}
import com.tosware.nkm.models.game.hex.*

import java.time.Instant
import scala.util.Random

object GameStateUpdateUtils extends GameStateUpdateUtils
trait GameStateUpdateUtils {
  implicit class GameStateUpdateUtils(gs: GameState) {
    def updateTimestamp(): GameState = gs.copy(lastTimestamp = Instant.now())

    def updateClock(newClock: Clock)(implicit random: Random, causedById: String): GameState =
      updateTimestamp()
        .copy(clock = newClock)
        .logEvent(ClockUpdated(gs.generateEventContext(), newClock))

    def updateGameStatus(newGameStatus: GameStatus)(implicit random: Random, causedById: String): GameState =
      gs.copy(gameStatus = newGameStatus)
        .logEvent(GameStatusUpdated(gs.generateEventContext(), newGameStatus))

    def updatePlayer(playerId: PlayerId)(updateFunction: Player => Player): GameState =
      gs.modify(_.players.each).using {
        case player if player.id == playerId => updateFunction(player)
        case player                          => player
      }

    def updateCharacter(characterId: CharacterId)(updateFunction: NkmCharacter => NkmCharacter): GameState =
      gs.modify(_.characters.each).using {
        case character if character.id == characterId => updateFunction(character)
        case character                                => character
      }

    def updateHexCell(targetCoords: HexCoordinates)(updateFunction: HexCell => HexCell): GameState =
      gs.modify(_.hexMap.cells.each).using {
        case cell if cell.coordinates == targetCoords => updateFunction(cell)
        case cell                                     => cell
      }

    def updateAbility(abilityId: AbilityId, newAbility: Ability): GameState =
      gs.modify(_.characters.each.state.abilities.each).using {
        case ability if ability.id == abilityId => newAbility
        case ability                            => ability
      }

  }
}
