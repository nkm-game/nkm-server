package com.tosware.nkm.models.game.abilities.roronoa_zoro

import com.tosware.nkm.models.game.Ability.AbilityId
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game._
import com.tosware.nkm.models.game.hex.{HexCoordinates, HexMap}
import com.tosware.nkm.models.game.hex.HexUtils._

import scala.annotation.tailrec
import scala.util.Random

object LackOfOrientation {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Lack of orientation",
      abilityType = AbilityType.Passive,
      description = "Character has a 50% chance to go get lost during basic move.",
    )
}

case class LackOfOrientation(abilityId: AbilityId, parentCharacterId: CharacterId) extends Ability(abilityId) with BasicMoveOverride {
  override val metadata = LackOfOrientation.metadata
  override val state = AbilityState(parentCharacterId)

  override def basicMove(path: Seq[HexCoordinates])(implicit random: Random, gameState: GameState): GameState = {
    implicit val hexMap: HexMap = gameState.hexMap.get

    @tailrec
    def generateLostPath(acc: Seq[HexCoordinates], coordsLeft: Int): Seq[HexCoordinates] = {
      if(acc.isEmpty) return Seq.empty
      if(coordsLeft <= 0) return acc
      val candidates = acc.last.getCircle(1).whereFreeToPass(parentCharacterId).filterNot(acc.contains)
      if(candidates.isEmpty) return acc
      val randomCandidate = random.shuffle(candidates.toSeq).head
      generateLostPath(acc :+ randomCandidate, coordsLeft - 1)
    }

    @tailrec
    def generateCorrectLostPath(): Seq[HexCoordinates] = {
      // TODO: make this less stupid
      val p = generateLostPath(Seq(path.head), path.size)
      if(p.last.toCell.isFreeToStand) p
      else generateCorrectLostPath()
    }

    val isLost = random.nextBoolean()
    val newPath = if(isLost) generateCorrectLostPath() else path

    parentCharacter.defaultBasicMove(newPath)
  }

}
