package com.tosware.nkm.models.game.abilities.roronoa_zoro

import com.tosware.nkm.*
import com.tosware.nkm.models.game.*
import com.tosware.nkm.models.game.ability.*
import com.tosware.nkm.models.game.hex.HexCoordinates
import spray.json.*

import scala.annotation.tailrec
import scala.util.Random

object LackOfOrientation extends NkmConf.AutoExtract {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Lack of orientation",
      abilityType = AbilityType.Passive,
      description = "50% chance to get lost during basic move.",
    )
  val timesMovedKey = "timesMoved"
  val timesLostKey = "timesLost"
}

case class LackOfOrientation(abilityId: AbilityId, parentCharacterId: CharacterId)
    extends Ability(abilityId, parentCharacterId)
    with BasicMoveOverride {
  import LackOfOrientation.*
  override val metadata = LackOfOrientation.metadata

  def timesMoved(implicit gameState: GameState): Int =
    state.variables.get(timesMovedKey)
      .map(_.parseJson.convertTo[Int])
      .getOrElse(0)

  def timesLost(implicit gameState: GameState): Int =
    state.variables.get(timesLostKey)
      .map(_.parseJson.convertTo[Int])
      .getOrElse(0)

  private def setTimesMoved(value: Int)(implicit random: Random, gameState: GameState): GameState =
    gameState.setAbilityVariable(id, timesMovedKey, value.toJson.toString)

  private def setTimesLost(value: Int)(implicit random: Random, gameState: GameState): GameState =
    gameState.setAbilityVariable(id, timesLostKey, value.toJson.toString)

  override def basicMove(path: Seq[HexCoordinates])(implicit random: Random, gameState: GameState): GameState = {
    @tailrec
    def generateLostPath(acc: Seq[HexCoordinates], coordsLeft: Int): Seq[HexCoordinates] = {
      if (acc.isEmpty) return Seq.empty
      if (coordsLeft <= 0) return acc
      val candidates = acc.last.getCircle(1).whereFreeToPass(parentCharacterId).filterNot(acc.contains)
      if (candidates.isEmpty) return acc
      val randomCandidate = random.shuffle(candidates.toSeq).head
      generateLostPath(acc :+ randomCandidate, coordsLeft - 1)
    }

    @tailrec
    def generateCorrectLostPath(): Seq[HexCoordinates] = {
      // TODO: make this less stupid
      val p = generateLostPath(Seq(path.head), path.size)
      if (p.last.toCell.isFreeToStand) p
      else generateCorrectLostPath()
    }

    val isLost = random.nextBoolean()
    val newPath = if (isLost) generateCorrectLostPath() else path

    val newTimesMoved = timesMoved + 1
    val newTimesLost = if (isLost) timesLost + 1 else timesLost

    val ngs = setTimesLost(newTimesLost)(random, setTimesMoved(newTimesMoved))

    parentCharacter.defaultBasicMove(newPath)(random, ngs)
  }

}
