package com.tosware.nkm.providers

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
import com.tosware.nkm.models.game.ability.AbilityMetadata
import com.tosware.nkm.serializers.NkmJsonProtocol

case class AbilityMetadatasProvider() extends NkmJsonProtocol {
  def getAbilityMetadatas: Seq[AbilityMetadata] = Seq(
    Murasame.metadata,
    Eliminate.metadata,
    LittleWarHorn.metadata,
    NaturesBeauty.metadata,
    Purification.metadata,
    Resurrection.metadata,
    CrackTheSky.metadata,
    MarkOfTheWind.metadata,
    SamuraisSwiftness.metadata,
    AceInTheHole.metadata,
    Check.metadata,
    Castling.metadata,
    ManipulatorOfObjects.metadata,
    BindingRibbons.metadata,
    TiamatsIntervention.metadata,
    BlackBlood.metadata,
    ScreechAlpha.metadata,
    Infection.metadata,
    WickedEyesServant.metadata,
    MjolnirHammer.metadata,
    MjolnirDestinyImpulse.metadata,
    Contact.metadata,
    Survivor.metadata,
    FinalSolution.metadata,
    MasterThrone.metadata,
    Aster.metadata,
    PowerOfExistence.metadata,
    Parry.metadata,
    Switch.metadata,
    StarburstStream.metadata,
    ImmenseHealingPowers.metadata,
    Invigorate.metadata,
    PowerOfTheGoddess.metadata,
    PChan.metadata,
    GrenadeThrow.metadata,
    RunItDown.metadata,
    RubberHuman.metadata,
    RubberRubberFruit.metadata,
    GearSecond.metadata,
    Mabinogion.metadata,
    SummerBreeze.metadata,
    FairyOfLove.metadata,
    ZeroGravity.metadata,
    ReducedWeight.metadata,
    SkillRelease.metadata,
    LackOfOrientation.metadata,
    OgreCutter.metadata,
    OneHundredEightPoundPhoenix.metadata,
    ScissorBlade.metadata,
    FiberDecapitation.metadata,
    GodrobeSenketsu.metadata,
    HighLuck.metadata,
    DrainTouch.metadata,
    Steal.metadata,
    WingsOfCrimson.metadata,
    GreatBladeOfCrimson.metadata,
    FinalBattleSecretTechnique.metadata,
    SnipersSight.metadata,
    TacticalEscape.metadata,
    PreciseShot.metadata,
  )
}
