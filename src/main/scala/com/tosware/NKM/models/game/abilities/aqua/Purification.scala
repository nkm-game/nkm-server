package com.tosware.NKM.models.game.abilities.aqua

import com.softwaremill.quicklens._
import com.tosware.NKM.models.{Damage, DamageType}
import com.tosware.NKM.models.game.NKMCharacter.CharacterId
import com.tosware.NKM.models.game._
import com.tosware.NKM.models.game.hex.HexUtils._

object Purification {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Purification",
      abilityType = AbilityType.Normal,
      description = "*Character* removes all negative effects from target.",
      cooldown = 4,
      range = 4,
    )
}

case class Purification(parentCharacterId: CharacterId) extends Ability with UsableOnCharacter {
  override def metadata = Purification.metadata
  override def state = AbilityState(parentCharacterId, metadata.cooldown)
  override def rangeCellCoords(implicit gameState: GameState) =
    parentCell.get.coordinates.getCircle(metadata.range).whereExists

  override def targetsInRange(implicit gameState: GameState) =
    rangeCellCoords.whereFriendsOf(parentCharacterId)

  override def use(target: CharacterId, useData: UseData)(implicit gameState: GameState) =
    gameState.updateCharacter(target, c => {
      c.modify(_.state.effects)
        .using(_.filterNot(_.metadata.effectType == CharacterEffectType.Negative))
    })


  // TODO: add validator that character is on map, target has negative effects
}
