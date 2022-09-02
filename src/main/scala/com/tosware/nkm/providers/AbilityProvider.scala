package com.tosware.nkm.providers

import com.tosware.nkm.models.game.Ability
import com.tosware.nkm.models.game.Ability.AbilityMetadataId
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game.abilities._
import com.tosware.nkm.models.game.hex.NkmUtils
import aqua._
import blank._
import hecate._
import llenn._
import roronoa_zoro._
import sinon._

import scala.util.Random

object AbilityProvider {
  def instantiateAbilities(characterId: CharacterId, metadataIds: Seq[AbilityMetadataId])(implicit random: Random): Seq[Ability] = {
    metadataIds.map {
      case NaturesBeauty.metadata.id =>
        NaturesBeauty(NkmUtils.randomUUID(), characterId)
      case Purification.metadata.id =>
        Purification(NkmUtils.randomUUID(), characterId)
      case Resurrection.metadata.id =>
        Resurrection(NkmUtils.randomUUID(), characterId)

      case AceInTheHole.metadata.id =>
        AceInTheHole(NkmUtils.randomUUID(), characterId)
      case Check.metadata.id =>
        Check(NkmUtils.randomUUID(), characterId)
      case Castling.metadata.id =>
        Castling(NkmUtils.randomUUID(), characterId)

      case MasterThrone.metadata.id =>
        MasterThrone(NkmUtils.randomUUID(), characterId)
      case Aster.metadata.id =>
        Aster(NkmUtils.randomUUID(), characterId)
      case PowerOfExistence.metadata.id =>
        PowerOfExistence(NkmUtils.randomUUID(), characterId)

      case SnipersSight.metadata.id =>
        SnipersSight(NkmUtils.randomUUID(), characterId)
      case TacticalEscape.metadata.id =>
        TacticalEscape(NkmUtils.randomUUID(), characterId)
      case PreciseShot.metadata.id =>
        PreciseShot(NkmUtils.randomUUID(), characterId)

      case PChan.metadata.id =>
        PChan(NkmUtils.randomUUID(), characterId)
      case GrenadeThrow.metadata.id =>
        GrenadeThrow(NkmUtils.randomUUID(), characterId)
      case RunItDown.metadata.id =>
        RunItDown(NkmUtils.randomUUID(), characterId)

      case LackOfOrientation.metadata.id =>
        LackOfOrientation(NkmUtils.randomUUID(), characterId)
      case OgreCutter.metadata.id =>
        OgreCutter(NkmUtils.randomUUID(), characterId)
      case OneHundredEightPoundPhoenix.metadata.id =>
        OneHundredEightPoundPhoenix(NkmUtils.randomUUID(), characterId)
    }
  }
}
