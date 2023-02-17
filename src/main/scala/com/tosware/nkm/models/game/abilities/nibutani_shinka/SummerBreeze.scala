package com.tosware.nkm.models.game.abilities.nibutani_shinka

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.ability.Ability.AbilityId
import com.tosware.nkm.models.game.character.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.ability.{Ability, AbilityMetadata, AbilityType, UsableOnCharacter, UseData}
import com.tosware.nkm.models.game.hex._

import scala.util.Random

object SummerBreeze {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Summer Breeze",
      abilityType = AbilityType.Normal,
      description =
        """Character summons Summer Breeze that knocks back selected enemy by {knockback}.
          |If the enemy will be knocked back into a wall or another character,
          |they will be stunned for {stunDuration}t and receive {damage} magical damage.
          |
          |Range: linear, {range}
          |""".stripMargin,
      variables = NkmConf.extract("abilities.nibutaniShinka.summerBreeze"),
      relatedEffectIds = Seq(effects.Stun.metadata.id),
    )
}

case class SummerBreeze(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId) with UsableOnCharacter {
  override val metadata: AbilityMetadata = SummerBreeze.metadata

  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] = ???

  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords.whereEnemiesOfC(parentCharacterId)

  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    ???
  }

}
