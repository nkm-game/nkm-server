package com.tosware.nkm.providers

import com.tosware.nkm.models.game.Ability
import com.tosware.nkm.models.game.Ability.AbilityMetadataId
import com.tosware.nkm.models.game.NkmCharacter.CharacterId
import com.tosware.nkm.models.game.abilities._
import akame._
import aqua._
import blank._
import carmel_wilhelmina._
import com.tosware.nkm.NkmUtils
import crona._
import hecate._
import kirito._
import llenn._
import roronoa_zoro._
import ryuko_matoi._
import sinon._

import scala.util.Random

object AbilityProvider {
  def instantiateAbilities(characterId: CharacterId, metadataIds: Seq[AbilityMetadataId])(implicit random: Random): Seq[Ability] = {
    metadataIds.map {
      case Murasame.metadata.id =>
        Murasame(NkmUtils.randomUUID(), characterId)
      case Eliminate.metadata.id =>
        Eliminate(NkmUtils.randomUUID(), characterId)
      case LittleWarHorn.metadata.id =>
        LittleWarHorn(NkmUtils.randomUUID(), characterId)

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

      case ManipulatorOfObjects.metadata.id =>
        ManipulatorOfObjects(NkmUtils.randomUUID(), characterId)
      case BindingRibbons.metadata.id =>
        BindingRibbons(NkmUtils.randomUUID(), characterId)
      case TiamatsIntervention.metadata.id =>
        TiamatsIntervention(NkmUtils.randomUUID(), characterId)

      case BlackBlood.metadata.id =>
        BlackBlood(NkmUtils.randomUUID(), characterId)
      case ScreechAlpha.metadata.id =>
        ScreechAlpha(NkmUtils.randomUUID(), characterId)
      case Infection.metadata.id =>
        Infection(NkmUtils.randomUUID(), characterId)

      case MasterThrone.metadata.id =>
        MasterThrone(NkmUtils.randomUUID(), characterId)
      case Aster.metadata.id =>
        Aster(NkmUtils.randomUUID(), characterId)
      case PowerOfExistence.metadata.id =>
        PowerOfExistence(NkmUtils.randomUUID(), characterId)

      case Parry.metadata.id =>
        Parry(NkmUtils.randomUUID(), characterId)
      case Switch.metadata.id =>
        Switch(NkmUtils.randomUUID(), characterId)
      case StarburstStream.metadata.id =>
        StarburstStream(NkmUtils.randomUUID(), characterId)

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

      case ScissorBlade.metadata.id =>
        ScissorBlade(NkmUtils.randomUUID(), characterId)
      case FiberDecapitation.metadata.id =>
        FiberDecapitation(NkmUtils.randomUUID(), characterId)
      case GodrobeSenketsu.metadata.id =>
        GodrobeSenketsu(NkmUtils.randomUUID(), characterId)

      case SnipersSight.metadata.id =>
        SnipersSight(NkmUtils.randomUUID(), characterId)
      case TacticalEscape.metadata.id =>
        TacticalEscape(NkmUtils.randomUUID(), characterId)
      case PreciseShot.metadata.id =>
        PreciseShot(NkmUtils.randomUUID(), characterId)
    }
  }
}
