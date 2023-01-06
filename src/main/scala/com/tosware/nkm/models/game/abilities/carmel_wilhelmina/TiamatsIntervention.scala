package com.tosware.nkm.models.game.abilities.carmel_wilhelmina

import com.tosware.nkm.{NkmConf, NkmUtils}
import com.tosware.nkm.models.game.Ability.{AbilityId, UseCheck}
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.effects.Stun
import com.tosware.nkm.models.game.hex.HexCoordinates
import spray.json._

import scala.util.Random

object TiamatsIntervention {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Tiamat's Intervention",
      abilityType = AbilityType.Ultimate,
      description =
        """Character pulls another character in range to a nearby position.
          |If it is an ally, it will gain {shield} shield.
          |If it is an enemy, it will be stunned for {stunDuration}t.
          |
          |Range: circular, {range}
          |Nearby position range: circular, {moveTargetRange}
          |""".stripMargin,
      variables = NkmConf.extract("abilities.carmelWilhelmina.tiamatsIntervention"),
      relatedEffectIds = Seq(Stun.metadata.id),
    )
}

case class TiamatsIntervention(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId, parentCharacterId) with UsableOnCharacter {
  override val metadata = TiamatsIntervention.metadata

  override def rangeCellCoords(implicit gameState: GameState) =
    parentCell.get.coordinates.getCircle(metadata.variables("range")).whereExists

  override def targetsInRange(implicit gameState: GameState) =
    rangeCellCoords.whereCharacters

  override def use(target: CharacterId, useData: UseData)(implicit random: Random, gameState: GameState) = {
    val targetCoords = useData.data.parseJson.convertTo[HexCoordinates]
    val gs = gameState.teleportCharacter(target, targetCoords)(random, id)
    if(parentCharacter.isEnemyForC(target)) {
      val stunEffect =  effects.Stun(NkmUtils.randomUUID(), metadata.variables("stunDuration"))
      gs.addEffect(target, stunEffect)(random, id)
    } else {
      gs.setShield(target, gs.characterById(target).state.shield + metadata.variables("shield"))(random, id)
    }
  }

  override def useChecks(implicit target: CharacterId, useData: UseData, gameState: GameState): Set[UseCheck] = {
    val targetCoords = useData.data.parseJson.convertTo[HexCoordinates]
    val nearbyFreeCells = parentCell.get.coordinates.getCircle(metadata.variables("moveTargetRange")).whereFreeToStand
    super.useChecks ++ Seq(
      nearbyFreeCells.nonEmpty -> "There are no nearby cells free to stand.",
      targetCoords.toCell(gameState.hexMap).isFreeToStand -> "Target cell is not free to stand.",
    )
  }
}
