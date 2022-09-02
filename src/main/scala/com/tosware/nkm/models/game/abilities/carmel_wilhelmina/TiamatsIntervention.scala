package com.tosware.nkm.models.game.abilities.carmel_wilhelmina

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.Ability.AbilityId
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._

import scala.util.Random

object TiamatsIntervention {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Tiamat's Intervention",
      abilityType = AbilityType.Ultimate,
      description =
        """Character pulls another character in range to a nearby position.
          |If it is an ally, it will gain {shield} shield.
          |If it is an enemy, it will be stunned for {stunDuration}t.
          |
          |Range: circular, {range}
          |Nearby position range: circular, {moveTargetRange}
          |""".stripMargin,
      variables = NkmConf.extract("abilities.carmelWilhelmina.tiamatsIntervention"),
    )
}

case class TiamatsIntervention(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId) with UsableOnCharacter {
  override val metadata = TiamatsIntervention.metadata
  override val state = AbilityState(parentCharacterId)
  override def rangeCellCoords(implicit gameState: GameState) = ???

  override def targetsInRange(implicit gameState: GameState) = ???

  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState) = ???
}
