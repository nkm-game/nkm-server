package com.tosware.nkm.models.game.abilities.crona

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.Ability.AbilityId
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._

import scala.util.Random

object BlackBlood {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Black Blood",
      abilityType = AbilityType.Passive,
      description =
        """After receiving damage, character deals {damage} magical damage to surrounding enemies.
          |
          |Range: circular, {range}""".stripMargin.format(),
      variables = NkmConf.extract("abilities.crona.blackBlood"),
    )
}

case class BlackBlood(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId) with GameEventListener {
  override val metadata = BlackBlood.metadata
  override val state = AbilityState(parentCharacterId)

  // NOTE: maybe instead onEvent we should add effect with onEvent, as ultimate reuses this
  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState = ???
}