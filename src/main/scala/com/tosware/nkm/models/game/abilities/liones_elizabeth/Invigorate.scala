package com.tosware.nkm.models.game.abilities.liones_elizabeth

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.hex.HexCoordinates

import scala.util.Random

object Invigorate extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Invigorate",
      abilityType = AbilityType.Normal,
      description =
        """Cast a spell on an ally that heals {heal} HP each turn for {duration}t.
          |
          |Range: circular, {range}""".stripMargin,
      relatedEffectIds = Seq(effects.HealOverTime.metadata.id),
      targetsMetadata = Seq(AbilityTargetMetadata.SingleCharacter),
    )
}

case class Invigorate(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId) with Usable {
  override val metadata: AbilityMetadata = Invigorate.metadata
  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    gameState.hexMap.cells.toCoords
  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords.whereFriendsOfC(parentCharacterId)
  override def use(useData: UseData)(implicit random: Random, gameState: GameState): GameState =
    gameState
      .addEffect(
        useData.firstAsCharacterId,
        effects.HealOverTime(randomUUID(), metadata.variables("duration"), metadata.variables("heal")),
      )(random, id)

  override def useChecks(implicit useData: UseData, gameState: GameState): Set[UseCheck] =
    super.useChecks
      ++ characterBaseUseChecks(useData.firstAsCharacterId) + UseCheck.Character.IsFriend(useData.firstAsCharacterId)
}
