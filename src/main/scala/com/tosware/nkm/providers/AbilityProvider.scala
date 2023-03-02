package com.tosware.nkm.providers

import com.tosware.nkm.models.game.ability.Ability.AbilityMetadataId
import com.tosware.nkm.models.game.ability.Ability
import com.tosware.nkm.models.game.character.NkmCharacter.CharacterId
import com.tosware.nkm.models.game.abilities._
import com.tosware.nkm.NkmUtils
import akame._
import aqua._
import ayatsuji_ayase._
import blank._
import carmel_wilhelmina._
import crona._
import ebisuzawa_kurumi._
import hecate._
import kirito._
import liones_elizabeth._
import llenn._
import nibutani_shinka._
import roronoa_zoro._
import ryuko_matoi._
import sinon._

import scala.util.Random

object AbilityProvider extends NkmUtils {
  def instantiateAbilities(characterId: CharacterId, metadataIds: Seq[AbilityMetadataId])(implicit random: Random): Seq[Ability] = {
    metadataIds.map {
      case Murasame.metadata.id =>
        Murasame(randomUUID(), characterId)
      case Eliminate.metadata.id =>
        Eliminate(randomUUID(), characterId)
      case LittleWarHorn.metadata.id =>
        LittleWarHorn(randomUUID(), characterId)

      case NaturesBeauty.metadata.id =>
        NaturesBeauty(randomUUID(), characterId)
      case Purification.metadata.id =>
        Purification(randomUUID(), characterId)
      case Resurrection.metadata.id =>
        Resurrection(randomUUID(), characterId)

      case SamuraisSwiftness.metadata.id =>
        SamuraisSwiftness(randomUUID(), characterId)
      case MarkOfTheWind.metadata.id =>
        MarkOfTheWind(randomUUID(), characterId)
      case CrackTheSky.metadata.id =>
        CrackTheSky(randomUUID(), characterId)

      case AceInTheHole.metadata.id =>
        AceInTheHole(randomUUID(), characterId)
      case Check.metadata.id =>
        Check(randomUUID(), characterId)
      case Castling.metadata.id =>
        Castling(randomUUID(), characterId)

      case ManipulatorOfObjects.metadata.id =>
        ManipulatorOfObjects(randomUUID(), characterId)
      case BindingRibbons.metadata.id =>
        BindingRibbons(randomUUID(), characterId)
      case TiamatsIntervention.metadata.id =>
        TiamatsIntervention(randomUUID(), characterId)

      case BlackBlood.metadata.id =>
        BlackBlood(randomUUID(), characterId)
      case ScreechAlpha.metadata.id =>
        ScreechAlpha(randomUUID(), characterId)
      case Infection.metadata.id =>
        Infection(randomUUID(), characterId)

      case Contact.metadata.id =>
        Contact(randomUUID(), characterId)
      case Survivor.metadata.id =>
        Survivor(randomUUID(), characterId)
      case FinalSolution.metadata.id =>
        FinalSolution(randomUUID(), characterId)

      case MasterThrone.metadata.id =>
        MasterThrone(randomUUID(), characterId)
      case Aster.metadata.id =>
        Aster(randomUUID(), characterId)
      case PowerOfExistence.metadata.id =>
        PowerOfExistence(randomUUID(), characterId)

      case Parry.metadata.id =>
        Parry(randomUUID(), characterId)
      case Switch.metadata.id =>
        Switch(randomUUID(), characterId)
      case StarburstStream.metadata.id =>
        StarburstStream(randomUUID(), characterId)

      case ImmenseHealingPowers.metadata.id =>
        ImmenseHealingPowers(randomUUID(), characterId)
      case Invigorate.metadata.id =>
        Invigorate(randomUUID(), characterId)
      case PowerOfTheGoddess.metadata.id =>
        PowerOfTheGoddess(randomUUID(), characterId)

      case PChan.metadata.id =>
        PChan(randomUUID(), characterId)
      case GrenadeThrow.metadata.id =>
        GrenadeThrow(randomUUID(), characterId)
      case RunItDown.metadata.id =>
        RunItDown(randomUUID(), characterId)

      case Mabinogion.metadata.id =>
        Mabinogion(randomUUID(), characterId)
      case SummerBreeze.metadata.id =>
        SummerBreeze(randomUUID(), characterId)
      case FairyOfLove.metadata.id =>
        FairyOfLove(randomUUID(), characterId)

      case LackOfOrientation.metadata.id =>
        LackOfOrientation(randomUUID(), characterId)
      case OgreCutter.metadata.id =>
        OgreCutter(randomUUID(), characterId)
      case OneHundredEightPoundPhoenix.metadata.id =>
        OneHundredEightPoundPhoenix(randomUUID(), characterId)

      case ScissorBlade.metadata.id =>
        ScissorBlade(randomUUID(), characterId)
      case FiberDecapitation.metadata.id =>
        FiberDecapitation(randomUUID(), characterId)
      case GodrobeSenketsu.metadata.id =>
        GodrobeSenketsu(randomUUID(), characterId)

      case SnipersSight.metadata.id =>
        SnipersSight(randomUUID(), characterId)
      case TacticalEscape.metadata.id =>
        TacticalEscape(randomUUID(), characterId)
      case PreciseShot.metadata.id =>
        PreciseShot(randomUUID(), characterId)
    }
  }
}
