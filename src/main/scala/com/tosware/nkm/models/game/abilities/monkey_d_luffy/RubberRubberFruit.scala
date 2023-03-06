package com.tosware.nkm.models.game.abilities.monkey_d_luffy

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.ability.Ability.AbilityId
import com.tosware.nkm.models.game.ability._
import com.tosware.nkm.models.game.character.NkmCharacter.CharacterId

import scala.util.Random

object RubberRubberFruit {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Rubber Rubber Fruit",
      alternateName = "ゴムゴムの実 (Gomu Gomu no Mi)",
      abilityType = AbilityType.Normal,
      description =
        """Character uses Devil Fruit power:
          |
          |<i>Enemy in range {bazookaRange}</i>
          |<b>Bazooka</b>
          |Character deals {bazookaDamage} physical damage and knocks back the enemy by {bazookaKnockback} tiles.
          |
          |<i>Enemy in further range</i>
          |<b>Pistol</b>
          |Character deals {pistolDamage} physical damage.
          |
          |<i>Wall</i>
          |<b>Rocket</b>
          |Character grabs a wall, jumping behind it as many squares as it has to the wall.
          |
          |This ability can be enchanted:
          |
          |<b>Bazooka</b>
          |Damage: {jetBazookaDamage}
          |Knockback: {jetBazookaKnockback}
          |
          |<b>Pistol</b>
          |Damage: {jetPistolDamage}
          |
          |Range: linear, {range}
          |Bazooka cooldown: {bazookaCooldown}""".stripMargin,
      variables = NkmConf.extract("abilities.monkey_d_luffy.rubberRubberFruit"),
    )
}

case class RubberRubberFruit(abilityId: AbilityId, parentCharacterId: CharacterId)
  extends Ability(abilityId, parentCharacterId)
    with Usable {
  override val metadata: AbilityMetadata = RubberRubberFruit.metadata

  override def use(useData: UseData)(implicit random: Random, gameState: GameState): GameState =
    gameState
}
