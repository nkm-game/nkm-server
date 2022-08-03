package com.tosware.NKM.models.game.abilities.aqua

import com.softwaremill.quicklens._
import com.tosware.NKM.models.{Damage, DamageType}
import com.tosware.NKM.models.game.NKMCharacter.CharacterId
import com.tosware.NKM.models.game._
import com.tosware.NKM.models.game.hex.HexUtils._

object NaturesBeauty {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Nature's Beauty",
      abilityType = AbilityType.Passive,
      description = "*Character* can use basic attacks on allies, healing them instead of dealing damage.",
    )
}

case class NaturesBeauty(parentCharacterId: CharacterId) extends Ability with BasicAttackOverride {
  override def metadata = NaturesBeauty.metadata
  override def state = AbilityState(parentCharacterId)
  override def rangeCellCoords(implicit gameState: GameState) =
    gameState.characterById(state.parentCharacterId).get.basicAttackCellCoords(gameState)
  override def targetsInRange(implicit gameState: GameState) =
    rangeCellCoords.whereFriendsOf(parentCharacterId)
  override def basicAttackCells(implicit gameState: GameState) = parentCharacter.defaultBasicAttackCells
  override def basicAttackTargets(implicit gameState: GameState) = parentCharacter.basicAttackTargets
  override def basicAttack(targetCharacterId: CharacterId)(implicit gameState: GameState) = {
    gameState.modify(_.players.each.characters.each).using {
      case character if character.id == targetCharacterId =>
        if(character.isFriendFor(parentCharacterId)) {
          character.heal(parentCharacter.state.attackPoints)
        } else {
          character.receiveDamage(Damage(id, DamageType.Physical, parentCharacter.state.attackPoints))
        }
      case character => character
    }
  }
}
