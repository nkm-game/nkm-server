package com.tosware.nkm.models.game.abilities.blank

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.Ability.AbilityId
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._

object AceInTheHole {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Ace In The Hole",
      abilityType = AbilityType.Passive,
      description = """If Character takes damage equal to more than X% of their maximum HP during the turn of one character, they will be able to use one of their abilities on their next move, regardless of its CD.
      It does not affect the actual ability CD count.""",
      variables = NkmConf.extract("abilities.blank.aceInTheHole"),
    )
}

case class AceInTheHole(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId) {
  override val metadata = AceInTheHole.metadata
  override val state = AbilityState(parentCharacterId)
}
