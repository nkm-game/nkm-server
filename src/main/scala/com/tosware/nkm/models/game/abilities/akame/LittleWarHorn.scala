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
      description =
        """Character gains {attackPoints} AD and {speedIncrease} Speed for {duration}t.
          |When this effect is finished, set this characters base Speed to {finalSpeed}.""".stripMargin,
      variables = NkmConf.extract("abilities.akame.littleWarHorn"),
    )
}

case class LittleWarHorn(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId) with UsableWithoutTarget {
  override val metadata = LittleWarHorn.metadata

  override def use()(implicit random: Random, gameState: GameState) = ???
}
