package com.tosware.nkm.models.game.effects

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.character_effect.*
import com.tosware.nkm.models.game.effects.BlackBlood.{sourceAbilityIdKey, sourceCharacterIdKey}
import com.tosware.nkm.models.game.event.GameEvent
import com.tosware.nkm.models.game.game_state.GameState

import scala.util.Random

object BlackBlood {
  def metadata: CharacterEffectMetadata =
    CharacterEffectMetadata(
      name = CharacterEffectName.BlackBlood,
      initialEffectType = CharacterEffectType.Mixed,
      description =
        """After receiving damage deal {damage} magical damage to surrounding enemies of this effect's caster.
          |
          |Radius: circular, {radius}""".stripMargin,
      isCc = true,
    )

  val sourceCharacterIdKey: String = "sourceCharacterId"
  val sourceAbilityIdKey: String = "sourceAbilityId"
}

case class BlackBlood(
    effectId: CharacterEffectId,
    initialCooldown: Int,
    sourceCharacterId: CharacterId,
    sourceAbilityId: AbilityId,
) extends CharacterEffect(effectId) {
  val metadata: CharacterEffectMetadata = BlackBlood.metadata

  override def effectType(implicit gameState: GameState): CharacterEffectType =
    if (parentCharacter.isFriendForC(sourceCharacterId)) CharacterEffectType.Positive
    else CharacterEffectType.Negative

  val radius: Int = abilities.crona.BlackBlood.metadata.variables("radius")
  val damage: Int = abilities.crona.BlackBlood.metadata.variables("damage")

  override def onInit()(implicit random: Random, gameState: GameState): GameState =
    gameState
      .setEffectVariable(id, sourceCharacterIdKey, sourceCharacterId)
      .setEffectVariable(id, sourceAbilityIdKey, sourceAbilityId)
      .setEffectVariable(id, "damage", damage)
      .setEffectVariable(id, "radius", radius)

  override def onEventReceived(e: GameEvent.GameEvent)(implicit random: Random, gameState: GameState): GameState =
    e match {
      case GameEvent.CharacterDamaged(context, characterId, _) =>
        if (context.causedById == sourceAbilityId) return gameState // activate only once, prevents infinite loop
        if (characterId != parentCharacter.id) return gameState
        if (!parentCharacter.isOnMap) return gameState

        val enemiesInRange =
          parentCell.map(_.coordinates
            .getCircle(radius)
            .whereEnemiesOfC(sourceCharacterId)
            .characters
            .map(_.id)).getOrElse(Set.empty)

        val dmg = Damage(DamageType.Magical, damage)
        enemiesInRange.foldLeft(gameState)((acc, cid) => hitCharacter(cid, dmg)(random, acc))

      case _ => gameState
    }
  def hitCharacter(target: CharacterId, damage: Damage)(implicit random: Random, gameState: GameState): GameState =
    if (gameState.characterById(target).isDead)
      gameState
    else
      gameState
        .abilityHitCharacter(sourceAbilityId, target)
        .damageCharacter(target, damage)(random, sourceAbilityId)

  override def description(implicit gameState: GameState): String =
    metadata.description
}
