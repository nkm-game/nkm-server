package com.tosware.nkm.models.game.abilities.liones_elizabeth

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.ability.Ability.AbilityId
import com.tosware.nkm.models.game.ability._
import com.tosware.nkm.models.game.character.NkmCharacter.CharacterId
import com.tosware.nkm.models.game.hex.HexCoordinates

import scala.util.Random

object PowerOfTheGoddess {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Power Of The Goddess",
      abilityType = AbilityType.Ultimate,
      description = "Character heals all friendly characters on the map for {heal} HP.",
      variables = NkmConf.extract("abilities.liones_elizabeth.powerOfTheGoddess"),
    )
}

case class PowerOfTheGoddess(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId) with UsableWithoutTarget {
  override val metadata: AbilityMetadata = PowerOfTheGoddess.metadata
  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    gameState.hexMap.cells.toCoords
  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords.whereFriendsOfC(parentCharacterId)

  override def use()(implicit random: Random, gameState: GameState): GameState = {
    val targets = targetsInRange.characters.map(_.id)
    targets.foldLeft(gameState)((acc, cid) => {
      acc.heal(cid, metadata.variables("heal"))(random, id)
    })
  }
}
