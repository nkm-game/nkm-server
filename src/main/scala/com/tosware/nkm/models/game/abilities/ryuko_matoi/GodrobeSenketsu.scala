package com.tosware.nkm.models.game.abilities.ryuko_matoi

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.Ability.AbilityId
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._

import scala.util.Random

object GodrobeSenketsu {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Godrobe Senketsu",
      alternateName = "神衣鮮血",
      abilityType = AbilityType.Ultimate,
      description =
        """Wear Senketsu, gaining flying effect and {initialAttackDamageBonus} AD.
          |At the end of this characters turn, gain additional {bonusAttackDamagePerTurn} and receive {damage} true damage.
          |You can move after using this ability.
          |
          |This ability can be disabled.
          |When disabled, you lose all bonus AD from this ability, stop receiving damage and the ability goes on cooldown.""".stripMargin,
      variables = NkmConf.extract("abilities.ryukoMatoi.godrobeSenketsu"),
    )
}

case class GodrobeSenketsu(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId) with UsableOnCharacter {
  override val metadata = GodrobeSenketsu.metadata

  override def rangeCellCoords(implicit gameState: GameState) = ???

  override def targetsInRange(implicit gameState: GameState) = ???

  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState) = ???
}
