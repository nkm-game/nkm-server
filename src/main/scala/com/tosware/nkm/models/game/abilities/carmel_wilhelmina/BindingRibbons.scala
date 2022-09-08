package com.tosware.nkm.models.game.abilities.carmel_wilhelmina

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.Ability.AbilityId
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.hex.HexCoordinates

import scala.util.Random

object BindingRibbons {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Binding Ribbons",
      abilityType = AbilityType.Normal,
      description =
        """Cast a spell that silents all hit enemies for {silenceDuration}t.
          |If it hits at least {enemiesToHitToActivateSnare} enemies, they will be rooted for {rootDuration}t.
          |
          |Range: circular, {range}
          |Radius: circular, {radius}""".stripMargin,
      variables = NkmConf.extract("abilities.carmelWilhelmina.bindingRibbons"),
    )
}

case class BindingRibbons(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId) with UsableOnCoordinates {
  override val metadata = BindingRibbons.metadata

  override def rangeCellCoords(implicit gameState: GameState) = ???

  override def use(target: HexCoordinates, useData: UseData)(implicit random: Random, gameState: GameState): GameState = ???
}
