package com.tosware.nkm.models.game.abilities.ayatsuji_ayase

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.GameState
import com.tosware.nkm.models.game.ability.Ability.AbilityId
import com.tosware.nkm.models.game.ability._
import com.tosware.nkm.models.game.character.NkmCharacter.CharacterId
import com.tosware.nkm.models.game.hex.HexCoordinates

import scala.util.Random

object CrackTheSky {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Crack the Sky",
      abilityType = AbilityType.Normal,
      description =
        "Character detonates selected traps, dealing {damage}+B physical damage to all hit enemies.",
      variables = NkmConf.extract("abilities.ayatsuji_ayase.crackTheSky"),
    )
}

case class CrackTheSky(abilityId: AbilityId, parentCharacterId: CharacterId)
  extends Ability(abilityId, parentCharacterId)
    with UsableOnCoordinates
{

  override val metadata: AbilityMetadata = MarkOfTheWind.metadata

  override def use(target: HexCoordinates, useData: UseData)(implicit random: Random, gameState: GameState): GameState = ???
}