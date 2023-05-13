package com.tosware.nkm.providers

import com.tosware.nkm.*
import com.tosware.nkm.models.game.abilities.akame.*
import com.tosware.nkm.models.game.abilities.aqua.*
import com.tosware.nkm.models.game.abilities.ayatsuji_ayase.*
import com.tosware.nkm.models.game.abilities.blank.*
import com.tosware.nkm.models.game.abilities.carmel_wilhelmina.*
import com.tosware.nkm.models.game.abilities.crona.*
import com.tosware.nkm.models.game.abilities.dekomori_sanae.*
import com.tosware.nkm.models.game.abilities.ebisuzawa_kurumi.*
import com.tosware.nkm.models.game.abilities.hecate.*
import com.tosware.nkm.models.game.abilities.kirito.*
import com.tosware.nkm.models.game.abilities.liones_elizabeth.*
import com.tosware.nkm.models.game.abilities.llenn.*
import com.tosware.nkm.models.game.abilities.monkey_d_luffy.*
import com.tosware.nkm.models.game.abilities.nibutani_shinka.*
import com.tosware.nkm.models.game.abilities.ochaco_uraraka.*
import com.tosware.nkm.models.game.abilities.roronoa_zoro.*
import com.tosware.nkm.models.game.abilities.ryuko_matoi.*
import com.tosware.nkm.models.game.abilities.satou_kazuma.*
import com.tosware.nkm.models.game.abilities.shana.*
import com.tosware.nkm.models.game.abilities.sinon.*
import com.tosware.nkm.models.game.ability.Ability

import scala.util.Random

object AbilityProvider {
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

      case WickedEyesServant.metadata.id =>
        WickedEyesServant(randomUUID(), characterId)
      case MjolnirHammer.metadata.id =>
        MjolnirHammer(randomUUID(), characterId)
      case MjolnirDestinyImpulse.metadata.id =>
        MjolnirDestinyImpulse(randomUUID(), characterId)

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

      case RubberHuman.metadata.id =>
        RubberHuman(randomUUID(), characterId)
      case RubberRubberFruit.metadata.id =>
        RubberRubberFruit(randomUUID(), characterId)
      case GearSecond.metadata.id =>
        GearSecond(randomUUID(), characterId)

      case Mabinogion.metadata.id =>
        Mabinogion(randomUUID(), characterId)
      case SummerBreeze.metadata.id =>
        SummerBreeze(randomUUID(), characterId)
      case FairyOfLove.metadata.id =>
        FairyOfLove(randomUUID(), characterId)

      case ZeroGravity.metadata.id =>
        ZeroGravity(randomUUID(), characterId)
      case ReducedWeight.metadata.id =>
        ReducedWeight(randomUUID(), characterId)
      case SkillRelease.metadata.id =>
        SkillRelease(randomUUID(), characterId)

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

      case HighLuck.metadata.id =>
        HighLuck(randomUUID(), characterId)
      case DrainTouch.metadata.id =>
        DrainTouch(randomUUID(), characterId)
      case Steal.metadata.id =>
        Steal(randomUUID(), characterId)

      case WingsOfCrimson.metadata.id =>
        WingsOfCrimson(randomUUID(), characterId)
      case GreatBladeOfCrimson.metadata.id =>
        GreatBladeOfCrimson(randomUUID(), characterId)
      case FinalBattleSecretTechnique.metadata.id =>
        FinalBattleSecretTechnique(randomUUID(), characterId)

      case SnipersSight.metadata.id =>
        SnipersSight(randomUUID(), characterId)
      case TacticalEscape.metadata.id =>
        TacticalEscape(randomUUID(), characterId)
      case PreciseShot.metadata.id =>
        PreciseShot(randomUUID(), characterId)
    }
  }
}
