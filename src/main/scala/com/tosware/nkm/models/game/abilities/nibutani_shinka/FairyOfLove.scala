package com.tosware.nkm.models.game.abilities.nibutani_shinka

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.ability.Ability.AbilityId
import com.tosware.nkm.models.game.character.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.ability.{Ability, AbilityMetadata, AbilityType, Usable, UseData}
import com.tosware.nkm.models.game.effects.AbilityEnchant

import scala.util.Random

object FairyOfLove {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Fairy Of Love",
      abilityType = AbilityType.Ultimate,
      description = "Character enchants the passive ability for {duration}t.",
      variables = NkmConf.extract("abilities.nibutani_shinka.fairyOfLove"),
      relatedEffectIds = Seq(effects.AbilityEnchant.metadata.id),
    )
}

case class FairyOfLove(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId) with Usable {
  override val metadata = FairyOfLove.metadata

  override def use(useData: UseData)(implicit random: Random, gameState: GameState): GameState =
    gameState.
      addEffect(parentCharacterId, AbilityEnchant(randomUUID(), metadata.variables("duration"), AbilityType.Passive))(random, id)
}
