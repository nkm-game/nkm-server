package com.tosware.nkm.models.game.abilities.ayatsuji_ayase

import com.tosware.nkm.NkmConf
import com.tosware.nkm.models.game.GameState
import com.tosware.nkm.models.game.ability.Ability.AbilityId
import com.tosware.nkm.models.game.ability._
import com.tosware.nkm.models.game.character.NkmCharacter.CharacterId
import com.tosware.nkm.models.game.hex.HexCoordinates

import scala.util.Random

object MarkOfTheWind {
  val metadata: AbilityMetadata =
    AbilityMetadata(
      name = "Mark of the Wind",
      abilityType = AbilityType.Normal,
      description =
        """Character sets up an invisible trap on a selected area.
          |
          |Range: circular, {range}
          |Trap radius: circular, {radius}
          |Max. number of traps: {trapLimit}""".stripMargin,
      variables = NkmConf.extract("abilities.ayatsuji_ayase.markOfTheWind"),
    )
  val setUpTrapsKey: String = "setUpTraps"
}

case class MarkOfTheWind(abilityId: AbilityId, parentCharacterId: CharacterId)
  extends Ability(abilityId, parentCharacterId)
    with UsableOnCoordinates
{

  override val metadata: AbilityMetadata = MarkOfTheWind.metadata

  override def use(target: HexCoordinates, useData: UseData)(implicit random: Random, gameState: GameState): GameState = ???
}