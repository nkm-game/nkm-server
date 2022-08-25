package com.tosware.NKM.providers

import com.tosware.NKM.models.game.Ability
import com.tosware.NKM.models.game.Ability.AbilityMetadataId
import com.tosware.NKM.models.game.NKMCharacter.CharacterId
import com.tosware.NKM.models.game.abilities._
import com.tosware.NKM.models.game.hex.NKMUtils
import aqua._
import hecate._
import llenn._
import roronoa_zoro._
import sinon._

import scala.util.Random

object AbilityProvider {
  def instantiateAbilities(characterId: CharacterId, metadataIds: Seq[AbilityMetadataId])(implicit random: Random): Seq[Ability] = {
    metadataIds.map {
      case NaturesBeauty.metadata.id =>
        NaturesBeauty(NKMUtils.randomUUID, characterId)
      case Purification.metadata.id =>
        Purification(NKMUtils.randomUUID, characterId)
      case Resurrection.metadata.id =>
        Resurrection(NKMUtils.randomUUID, characterId)

      case MasterThrone.metadata.id =>
        MasterThrone(NKMUtils.randomUUID, characterId)
      case Aster.metadata.id =>
        Aster(NKMUtils.randomUUID, characterId)
      case PowerOfExistence.metadata.id =>
        PowerOfExistence(NKMUtils.randomUUID, characterId)

      case SnipersSight.metadata.id =>
        SnipersSight(NKMUtils.randomUUID, characterId)
      case TacticalEscape.metadata.id =>
        TacticalEscape(NKMUtils.randomUUID, characterId)
      case PreciseShot.metadata.id =>
        PreciseShot(NKMUtils.randomUUID, characterId)

      case PChan.metadata.id =>
        PChan(NKMUtils.randomUUID, characterId)
      case GrenadeThrow.metadata.id =>
        GrenadeThrow(NKMUtils.randomUUID, characterId)
      case RunItDown.metadata.id =>
        RunItDown(NKMUtils.randomUUID, characterId)

      case LackOfOrientation.metadata.id =>
        LackOfOrientation(NKMUtils.randomUUID, characterId)
      case OgreCutter.metadata.id =>
        OgreCutter(NKMUtils.randomUUID, characterId)
      case OneHundredEightPoundPhoenix.metadata.id =>
        OneHundredEightPoundPhoenix(NKMUtils.randomUUID, characterId)
    }
  }
}
