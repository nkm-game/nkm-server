package com.tosware.nkm.models.game.game_state.extensions

import com.softwaremill.quicklens.*
import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.character.*
import com.tosware.nkm.models.game.event.*
import com.tosware.nkm.models.game.event.GameEvent.*
import com.tosware.nkm.models.game.game_state.GameState
import com.tosware.nkm.models.game.hex.*

import scala.util.Random

object GameStateCharacterUtils extends GameStateCharacterUtils
trait GameStateCharacterUtils {
  implicit class GameStateCharacterUtils(gs: GameState) {
    def getDirection(from: CharacterId, to: CharacterId)(implicit gameState: GameState): Option[HexDirection] =
      for {
        fromCoordinates: HexCoordinates <- gs.characterById(from).parentCellOpt.map(_.coordinates)
        toCoordinates: HexCoordinates <- gs.characterById(to).parentCellOpt.map(_.coordinates)
        direction: HexDirection <- fromCoordinates.getDirection(toCoordinates)
      } yield direction

    def removeCharacterFromMap(characterId: CharacterId)(implicit random: Random, causedById: String): GameState = {
      val parentCellOpt = gs.characterById(characterId).parentCellOpt(gs)

      parentCellOpt.fold(gs)(c => gs.updateHexCell(c.coordinates)(_.copy(characterId = None)))
        .modify(_.characterIdsOutsideMap).setTo(gs.characterIdsOutsideMap + characterId)
        .logEvent(CharacterRemovedFromMap(randomUUID(), gs.phase, gs.turn, causedById, characterId))
    }

    def swapCharacters(characterId1: CharacterId, characterId2: CharacterId)(
        implicit
        random: Random,
        gameState: GameState,
        causedById: String,
    ): GameState = {
      val character1 = gs.characterById(characterId1)
      val character2 = gs.characterById(characterId2)
      removeCharacterFromMap(character1.id)
        .removeCharacterFromMap(character2.id)
        .placeCharacter(character2.parentCellOpt.get.coordinates, character1.id)
        .placeCharacter(character1.parentCellOpt.get.coordinates, character2.id)
    }

    def takeActionWithCharacter(characterId: CharacterId)(implicit random: Random): GameState = {
      implicit val causedById: String = characterId

      if (gs.characterTakingActionThisTurnOpt.isDefined) // do not log event more than once
        return gs
      gs.modify(_.characterTakingActionThisTurnOpt)
        .setTo(Some(characterId))
        .logEvent(CharacterTookAction(randomUUID(), gs.phase, gs.turn, causedById, characterId))
    }

    def setHp(characterId: CharacterId, amount: Int)(implicit random: Random, causedById: String): GameState =
      gs.updateCharacter(characterId)(_.modify(_.state.healthPoints).setTo(amount))
        .logEvent(CharacterHpSet(randomUUID(), gs.phase, gs.turn, causedById, characterId, amount))

    def setShield(characterId: CharacterId, amount: Int)(implicit random: Random, causedById: String): GameState =
      gs.updateCharacter(characterId)(_.modify(_.state.shield).setTo(amount))
        .logEvent(CharacterShieldSet(randomUUID(), gs.phase, gs.turn, causedById, characterId, amount))

    def setAttackType(characterId: CharacterId, attackType: AttackType)(implicit
        random: Random,
        causedById: String,
    ): GameState =
      gs.updateCharacter(characterId)(_.modify(_.state.attackType).setTo(attackType))
        .logEvent(CharacterAttackTypeSet(randomUUID(), gs.phase, gs.turn, causedById, characterId, attackType))

    def setStat(characterId: CharacterId, statType: StatType, amount: Int)(implicit
        random: Random,
        causedById: String,
    ): GameState = {
      val updateStat = statType match {
        case StatType.AttackPoints     => modify(_: NkmCharacter)(_.state.pureAttackPoints)
        case StatType.BasicAttackRange => modify(_: NkmCharacter)(_.state.pureBasicAttackRange)
        case StatType.Speed            => modify(_: NkmCharacter)(_.state.pureSpeed)
        case StatType.PhysicalDefense  => modify(_: NkmCharacter)(_.state.purePhysicalDefense)
        case StatType.MagicalDefense   => modify(_: NkmCharacter)(_.state.pureMagicalDefense)
      }
      gs.updateCharacter(characterId)(c => updateStat(c).setTo(amount))
        .logEvent(CharacterStatSet(randomUUID(), gs.phase, gs.turn, causedById, characterId, statType, amount))
    }

    def knockbackCharacter(
        characterId: CharacterId,
        direction: HexDirection,
        knockback: Int,
    )(implicit random: Random, causedById: String): (GameState, KnockbackResult) = {
      def determineResult(blockedCellOption: Option[HexCell]): KnockbackResult =
        blockedCellOption match {
          case Some(blocked) if blocked.isWall => KnockbackResult.HitWall
          case Some(_)                         => KnockbackResult.HitCharacter
          case None                            => KnockbackResult.HitNothing
        }

      def computeCellToTeleport(lineCells: Seq[HexCell], blockedCellOption: Option[HexCell]): Option[HexCell] =
        blockedCellOption match {
          case Some(blockedCell) =>
            val cellsBeforeBlockage = lineCells.takeWhile(_ != blockedCell)
            cellsBeforeBlockage.lastOption

          case None =>
            lineCells.lastOption
        }

      val targetCellOption = gs.hexMap.getCellOfCharacter(characterId)
      val defaultResult = if (targetCellOption.isEmpty) KnockbackResult.HitNothing else KnockbackResult.HitEndOfMap

      val result = for {
        targetCell <- targetCellOption
        lineCells = targetCell.getLine(direction, knockback)(gs)
        if lineCells.nonEmpty
        firstBlockedCellOption = lineCells.find(!_.isFreeToStand)
        cellToTeleport <- computeCellToTeleport(lineCells, firstBlockedCellOption)
      } yield {
        val result = determineResult(firstBlockedCellOption)
        val teleportGs = gs.teleportCharacter(characterId, cellToTeleport.coordinates)
        (teleportGs, result)
      }

      result.getOrElse((gs, defaultResult))
    }

    def jump(
        characterId: CharacterId,
        direction: HexDirection,
        amount: Int,
    )(implicit random: Random, causedById: String): GameState = {
      val parentCellOpt = gs.hexMap.getCellOfCharacter(characterId)
      parentCellOpt.fold(gs) { parentCell =>
        val lineCells: Seq[HexCell] = parentCell.getLine(direction, amount)(gs)
        val cellToTeleport = lineCells.findLast(_.isFreeToStand).getOrElse(parentCell)
        gs.teleportCharacter(characterId, cellToTeleport.coordinates)(random, gs.id)
      }
    }

    def heal(characterId: CharacterId, amount: Int)(implicit random: Random, causedById: String): GameState = {
      if (gs.characterById(characterId).isDead) {
        gs.logger.error(s"Unable to heal character $characterId. Character dead.")
        return gs
      }
      val healPreparedId = randomUUID()
      val healPreparedGs = gs.logEvent(HealPrepared(healPreparedId, gs.phase, gs.turn, causedById, characterId, amount))

      val additionalHealing =
        healPreparedGs
          .gameLog
          .events
          .ofType[HealAmplified]
          .filter(_.healPreparedId == healPreparedId)
          .map(_.additionalAmount)
          .sum

      val resultHealing = amount + additionalHealing

      healPreparedGs
        .updateCharacter(characterId)(_.heal(resultHealing))
        .logEvent(CharacterHealed(randomUUID(), gs.phase, gs.turn, causedById, characterId, resultHealing))
    }

    def moveToClosestFreeCell(characterId: CharacterId)(implicit random: Random, causedById: String): GameState =
      gs.hexMap.getCellOfCharacter(characterId) match {
        case Some(sourceCell) =>
          sourceCell.findClosestFreeCell(gs, random) match {
            case Some(closestFreeCell) => gs.teleportCharacter(characterId, closestFreeCell.coordinates)
            case None                  => gs
          }
        case None => gs
      }

    def refreshAnything(targetCharacter: CharacterId)(implicit random: Random, causedById: String): GameState =
      gs.logEvent(AnythingRefreshed(randomUUID(), gs.phase, gs.turn, causedById, targetCharacter))

    def refreshBasicMove(targetCharacter: CharacterId)(implicit random: Random, causedById: String): GameState =
      gs.logEvent(BasicMoveRefreshed(randomUUID(), gs.phase, gs.turn, causedById, targetCharacter))

    def refreshBasicAttack(targetCharacter: CharacterId)(implicit random: Random, causedById: String): GameState =
      gs.logEvent(BasicAttackRefreshed(randomUUID(), gs.phase, gs.turn, causedById, targetCharacter))

    def damageCharacter(characterId: CharacterId, damage: Damage)(
        implicit
        random: Random,
        causedById: String,
    ): GameState = {
      if (gs.characterById(characterId).isDead) {
        gs.logger.error(s"Unable to damage character $characterId. Character dead.")
        return gs
      }
      val damagePreparedId = randomUUID()
      val damagePreparedGs =
        gs.logEvent(DamagePrepared(damagePreparedId, gs.phase, gs.turn, causedById, characterId, damage))

      val additionalDamage =
        damagePreparedGs
          .gameLog
          .events
          .ofType[DamageAmplified]
          .filter(_.damagePreparedId == damagePreparedId)
          .map(_.additionalAmount)
          .sum

      val resultDamage = damage.copy(amount = Math.max(0, damage.amount + additionalDamage))

      if (resultDamage.amount <= 0)
        return damagePreparedGs

      damagePreparedGs
        .logEvent(DamageSent(randomUUID(), gs.phase, gs.turn, causedById, characterId, resultDamage))
        .applyDamageToCharacter(characterId, resultDamage)
    }

    def amplifyDamage(damagePreparedId: GameEventId, additionalAmount: Int)(
        implicit
        random: Random,
        causedById: String,
    ): GameState =
      if (additionalAmount == 0)
        gs
      else
        gs.logEvent(DamageAmplified(randomUUID(), gs.phase, gs.turn, causedById, damagePreparedId, additionalAmount))

    def amplifyHeal(healPreparedId: GameEventId, additionalAmount: Int)(
        implicit
        random: Random,
        causedById: String,
    ): GameState =
      if (additionalAmount == 0)
        gs
      else
        gs.logEvent(HealAmplified(randomUUID(), gs.phase, gs.turn, causedById, healPreparedId, additionalAmount))

    def basicMoveOneCell(characterId: CharacterId, targetCellCoordinates: HexCoordinates)(
        implicit
        random: Random,
        causedById: String,
    ): GameState =
      teleportCharacter(characterId, targetCellCoordinates, hideEvent = true)

    def teleportCharacter(
        characterId: CharacterId,
        targetCellCoordinates: HexCoordinates,
        hideEvent: Boolean = false,
    )(implicit random: Random, causedById: String): GameState = {
      val targetCell = gs.hexMap.getCell(targetCellCoordinates) match {
        case Some(value) => value
        case None        => return gs
      }

      val characterToTp = gs.characterById(characterId)
      val characterIsOnMap = characterToTp.isOnMap(gs)

      val parentCellOpt = gs.characterById(characterId).parentCellOpt(gs)
      val removedFromParentCellGs =
        parentCellOpt.fold(gs)(c => gs.updateHexCell(c.coordinates)(_.copy(characterId = None)))

      val ngs = if (!targetCell.isFreeToStand) {
        val targetStandingOpt = targetCell.characterOpt(gs)
        if (targetStandingOpt.isEmpty) return gs // probably a bug
        val isFriendStanding = targetStandingOpt.exists(_.isFriendFor(characterId)(gs))
        if (isFriendStanding) {
          // probably just passing by
          removedFromParentCellGs.removeCharacterFromMap(characterId)
        } else {
          // probably an invisible character standing in a way
          val targetStanding = targetStandingOpt.get
          val tpInterruptedEvent =
            GameEvent.MovementInterrupted(randomUUID(), gs.phase, gs.turn, targetStanding.id, characterId)

          val targetMovedGs = removedFromParentCellGs
            .logEvent(tpInterruptedEvent)
            .moveToClosestFreeCell(targetStanding.id)

          if (characterIsOnMap)
            targetMovedGs.updateHexCell(targetCellCoordinates)(_.copy(characterId = Some(characterId)))
          else targetMovedGs.placeCharacter(targetCellCoordinates, characterId)
        }
      } else {
        if (characterIsOnMap)
          removedFromParentCellGs.updateHexCell(targetCellCoordinates)(_.copy(characterId = Some(characterId)))
        else removedFromParentCellGs.placeCharacter(targetCellCoordinates, characterId)
      }

      val tpEvent = CharacterTeleported(randomUUID(), gs.phase, gs.turn, causedById, characterId, targetCellCoordinates)
      if (hideEvent) {
        ngs.logAndHideEvent(tpEvent, Seq(), RevealCondition.Never)
      } else {
        ngs.logEvent(tpEvent)
      }
    }

    def executeCharacter(characterId: CharacterId)(implicit random: Random, causedById: String): GameState =
      damageCharacter(characterId, Damage(DamageType.True, Int.MaxValue))

    def applyDamageToCharacter(characterId: CharacterId, damage: Damage)(
        implicit
        random: Random,
        causedById: String,
    ): GameState = {
      val c = gs.characterById(characterId)

      val reduction = c.calculateReduction(damage)
      val damageAfterReduction: Int = damage.amount - reduction
      if (damageAfterReduction <= 0)
        return gs

      if (c.state.shield >= damageAfterReduction) {
        val newShield = c.state.shield - damageAfterReduction
        gs.updateCharacter(characterId)(_.modify(_.state.shield).setTo(newShield))
          .logEvent(ShieldDamaged(randomUUID(), gs.phase, gs.turn, causedById, characterId, damageAfterReduction))
      } else {
        val damageAfterShield = damageAfterReduction - c.state.shield
        val newShield = 0
        val newHp = c.state.healthPoints - damageAfterShield
        val stateChangedGs = gs.updateCharacter(characterId)(_
          .modify(_.state.shield).setTo(newShield)
          .modify(_.state.healthPoints).setTo(newHp))
        val shieldDamagedEventGs =
          if (c.state.shield > 0)
            stateChangedGs
              .logEvent(ShieldDamaged(randomUUID(), gs.phase, gs.turn, causedById, characterId, c.state.shield))
          else stateChangedGs

        shieldDamagedEventGs
          .checkIfCharacterDied(
            characterId
          ) // needs to be removed from map before logging an event in order to avoid infinite triggers
          .logEvent(CharacterDamaged(randomUUID(), gs.phase, gs.turn, causedById, characterId, damageAfterShield))
      }
    }

  }
}
