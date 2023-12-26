package com.tosware.nkm.models.game.game_state.extensions

import com.tosware.nkm.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.character.*
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.pick.PickType

import scala.util.Random

object GameStateInitialization extends GameStateInitialization
trait GameStateInitialization {
  implicit class GameStateInitialization(gs: GameState) {
    def initializeCharacterPick()(implicit random: Random): GameState = {
      val characterPickInitialPickTime: Long = gs.pickType match {
        case PickType.AllRandom => gs.clockConfig.timeAfterPickMillis
        case PickType.DraftPick => gs.clockConfig.maxBanTimeMillis
        case PickType.BlindPick => gs.clockConfig.maxPickTimeMillis
      }
      gs.updateClock(gs.clock.setSharedTime(characterPickInitialPickTime))(random, gs.id)
    }

    def initializeCharacterPlacing()(implicit random: Random): GameState =
      gs.updateClock(gs.clock.setSharedTime(gs.clockConfig.timeForCharacterPlacing))(random, gs.id)

    def placeCharactersRandomly(forPlayers: Set[PlayerId])(implicit random: Random, causedById: String): GameState =
      gs.players.filter(pl => forPlayers.contains(pl.id)).foldLeft(gs) {
        case (acc, p) =>
          val spawnCoords = gs.hexMap.getSpawnPointsFor(p.id)(gs).map(_.coordinates)
          val characterIdsShuffled = random.shuffle(p.characterIds.toSeq)
          val coordinatesToCharacterIdMap = spawnCoords.zip(characterIdsShuffled).toMap
          acc.placeCharacters(p.id, coordinatesToCharacterIdMap)
      }

    def pickAndPlaceCharactersRandomlyIfAllRandom()(implicit random: Random, causedById: String): GameState =
      if (gs.pickType == PickType.AllRandom)
        gs.assignCharactersToPlayers()
          .placeCharactersRandomly(gs.players.map(_.id).toSet)
      else gs

    def generateCharacter(characterMetadataId: CharacterMetadataId)(implicit random: Random): NkmCharacter = {
      val characterId = randomUUID()
      val metadata = gs.charactersMetadata.find(_.id == characterMetadataId).get
      NkmCharacter.fromMetadata(characterId, metadata)
    }

    def assignCharactersToPlayers()(implicit random: Random): GameState = {
      val characterSelection: Map[PlayerId, Iterable[CharacterMetadataId]] = gs.pickType match {
        case PickType.AllRandom =>
          val pickedCharacters = random
            .shuffle(gs.charactersMetadata.map(_.id).toSeq)
            .grouped(gs.numberOfCharactersPerPlayers)
            .take(gs.players.length)
          gs.players.map(_.id).zip(pickedCharacters).toMap
        case PickType.DraftPick =>
          gs.draftPickStateOpt.map(_.characterSelection).getOrElse(Map.empty)
        case PickType.BlindPick =>
          gs.blindPickStateOpt.map(_.characterSelection).getOrElse(Map.empty)
      }

      val playersWithCharacters =
        gs.players.map { p =>
          val generatedCharacters = characterSelection(p.id).map(c => generateCharacter(c)).toSet
          (p, generatedCharacters)
        }
      val playersWithAssignedCharacters = playersWithCharacters.map { case (p, cs) =>
        p.copy(characterIds = cs.map(_.id))
      }
      val characters = playersWithCharacters.flatMap(_._2).toSet

      gs.copy(
        players = playersWithAssignedCharacters,
        characters = characters,
        characterIdsOutsideMap = characters.map(c => c.id),
      ).initAbilityState()
    }

    def initAbilityState(): GameState = {
      val abilitiesByCharacter = gs.characters.map(c => (c.id, c.state.abilities))
      val abilityStatesMap: Map[AbilityId, AbilityState] = abilitiesByCharacter.collect {
        case (_: CharacterId, as: Seq[Ability]) =>
          as.map(a => a.id -> AbilityState(variables = a.metadata.variables.map { case (s, i) => (s, i.toString) }))
      }.flatten.toMap

      gs.copy(
        abilityStates = gs.abilityStates.concat(abilityStatesMap)
      )
    }

  }

}
