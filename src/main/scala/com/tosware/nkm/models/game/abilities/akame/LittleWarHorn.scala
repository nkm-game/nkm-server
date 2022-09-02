package com.tosware.nkm.models.game.abilities.akame

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.Ability.AbilityId
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._

import scala.util.Random

object LittleWarHorn {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Little War Horn",
      abilityType = AbilityType.Ultimate,
      description = "Character gains 10 AttackPoints and 4 Speed for 5 turns. After effect finish set *character's* Speed to 5.",
      variables = NkmConf.extract("abilities.akame.littleWarHorn"),
    )
}

case class LittleWarHorn(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId) with UsableWithoutTarget {
  override val metadata = LittleWarHorn.metadata
  override val state = AbilityState(parentCharacterId)

  override def use()(implicit random: Random, gameState: GameState) = ???
}
