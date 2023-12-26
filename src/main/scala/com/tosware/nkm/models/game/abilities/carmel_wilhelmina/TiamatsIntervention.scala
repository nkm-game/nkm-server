package com.tosware.nkm.models.game.abilities.carmel_wilhelmina

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.effects.Stun
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.HexCoordinates

import scala.util.Random

object TiamatsIntervention extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Tiamat's Intervention",
      abilityType = AbilityType.Ultimate,
      description =
        """Pull a character to a nearby position.
          |If it is an ally, give them {shield} Shield.
          |If it is an enemy, Stun them for {stunDuration}t.
          |
          |Range: circular, {range}
          |Nearby position range: circular, {moveTargetRange}""".stripMargin,
      relatedEffectIds = Seq(Stun.metadata.id),
      targetsMetadata = Seq(AbilityTargetMetadata.SingleCharacter),
    )
}

case class TiamatsIntervention(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId) with Usable {
  override val metadata: AbilityMetadata = TiamatsIntervention.metadata

  override def rangeCellCoords(implicit gameState: GameState): Set[HexCoordinates] =
    parentCell.fold(Set.empty[HexCoordinates])(_.coordinates.getCircle(metadata.variables("range")).whereExists)

  override def targetsInRange(implicit gameState: GameState): Set[HexCoordinates] =
    rangeCellCoords.whereCharacters

  override def use(useData: UseData)(implicit random: Random, gameState: GameState): GameState = {
    val targetCharacter = useData.firstAsCharacterId
    val targetCoords = useData.secondAsCoordinates
    val teleportGs = gameState.teleportCharacter(targetCharacter, targetCoords)(random, id)
    if (parentCharacter.isEnemyForC(targetCharacter)) {
      val stunEffect = effects.Stun(randomUUID(), metadata.variables("stunDuration"))
      teleportGs.addEffect(targetCharacter, stunEffect)(random, id)
    } else {
      teleportGs.setShield(
        targetCharacter,
        teleportGs.characterById(targetCharacter).state.shield + metadata.variables("shield"),
      )(
        random,
        id,
      )
    }
  }

  override def useChecks(implicit useData: UseData, gameState: GameState): Set[UseCheck] = {
    val targetCharacter = useData.firstAsCharacterId
    val targetCoords = useData.secondAsCoordinates
    val nearbyFreeCells = parentCell.get.coordinates.getCircle(metadata.variables("moveTargetRange")).whereFreeToStand

    super.useChecks ++ characterBaseUseChecks(targetCharacter) ++ Set(
      UseCheck.Coordinates.IsFreeToStand(targetCoords),
      UseCheck.Coordinates.ExistsOnMap(targetCoords),
      UseCheck.Coordinates.InRangeOf(nearbyFreeCells, targetCoords),
    )
  }
}
