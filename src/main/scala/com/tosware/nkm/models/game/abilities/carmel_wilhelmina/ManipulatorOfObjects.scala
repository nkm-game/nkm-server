package com.tosware.nkm.models.game.abilities.carmel_wilhelmina

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.Ability.AbilityId
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._

import scala.util.Random

object ManipulatorOfObjects {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Manipulator Of Objects",
      abilityType = AbilityType.Passive,
      description = """Basic attacks of this character root enemies for {duration}t.
      |This effect cannot be added on the same enemy for {effectTimeout}t.""".stripMargin,
      variables = NkmConf.extract("abilities.carmelWilhelmina.manipulatorOfObjects"),
    )
}

case class ManipulatorOfObjects(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId) with GameEventListener {
  override val metadata = ManipulatorOfObjects.metadata


  override def onEvent(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState = ???
}
