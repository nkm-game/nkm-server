package com.tosware.nkm.providers

import com.tosware.nkm.models.game.abilities._
import akame._
import aqua._
import blank._
import carmel_wilhelmina._
import crona._
import hecate._
import llenn._
import roronoa_zoro._
import sinon._
import com.tosware.nkm.models.game.{AttackType, CharacterMetadata}
import com.tosware.nkm.serializers.NkmJsonProtocol

case class CharacterMetadatasProvider() extends NkmJsonProtocol {

  def getCharacterMetadatas: Seq[CharacterMetadata] = Seq(
    CharacterMetadata(
      name = "Aqua",
      attackType = AttackType.Ranged,
      initialHealthPoints = 58,
      initialAttackPoints = 8,
      initialBasicAttackRange = 5,
      initialSpeed = 5,
      initialPsychicalDefense = 15,
      initialMagicalDefense = 20,
      initialAbilitiesMetadataIds = Seq(
        NaturesBeauty.metadata.id,
        Purification.metadata.id,
        Resurrection.metadata.id,
      )
    ),
    CharacterMetadata(
      name = "Asuna",
      attackType = AttackType.Melee,
      initialHealthPoints = 66,
      initialAttackPoints = 12,
      initialBasicAttackRange = 4,
      initialSpeed = 6,
      initialPsychicalDefense = 20,
      initialMagicalDefense = 10,
      initialAbilitiesMetadataIds = Seq.empty
    ),
    CharacterMetadata(
      name = "Dekomori Sanae",
      attackType = AttackType.Ranged,
      initialHealthPoints = 46,
      initialAttackPoints = 16,
      initialBasicAttackRange = 7,
      initialSpeed = 5,
      initialPsychicalDefense = 10,
      initialMagicalDefense = 15,
      initialAbilitiesMetadataIds = Seq.empty
    ),
    CharacterMetadata(
      name = "Gilgamesh",
      attackType = AttackType.Ranged,
      initialHealthPoints = 51,
      initialAttackPoints = 11,
      initialBasicAttackRange = 6,
      initialSpeed = 5,
      initialPsychicalDefense = 15,
      initialMagicalDefense = 15,
      initialAbilitiesMetadataIds = Seq.empty
    ),
    CharacterMetadata(
      name = "Hanekawa Tsubasa",
      attackType = AttackType.Melee,
      initialHealthPoints = 70,
      initialAttackPoints = 14,
      initialBasicAttackRange = 2,
      initialSpeed = 7,
      initialPsychicalDefense = 15,
      initialMagicalDefense = 15,
      initialAbilitiesMetadataIds = Seq.empty
    ),
    CharacterMetadata(
      name = "Hecate",
      attackType = AttackType.Ranged,
      initialHealthPoints = 59,
      initialAttackPoints = 11,
      initialBasicAttackRange = 7,
      initialSpeed = 3,
      initialPsychicalDefense = 5,
      initialMagicalDefense = 20,
      initialAbilitiesMetadataIds = Seq(
        MasterThrone.metadata.id,
        Aster.metadata.id,
        PowerOfExistence.metadata.id,
      )
    ),
    CharacterMetadata(
      name = "Rem",
      attackType = AttackType.Melee,
      initialHealthPoints = 54,
      initialAttackPoints = 12,
      initialBasicAttackRange = 6,
      initialSpeed = 5,
      initialPsychicalDefense = 10,
      initialMagicalDefense = 15,
      initialAbilitiesMetadataIds = Seq.empty
    ),
    CharacterMetadata(
      name = "Roronoa Zoro",
      attackType = AttackType.Melee,
      initialHealthPoints = 87,
      initialAttackPoints = 27,
      initialBasicAttackRange = 2,
      initialSpeed = 8,
      initialPsychicalDefense = 40,
      initialMagicalDefense = 25,
      initialAbilitiesMetadataIds = Seq(
        LackOfOrientation.metadata.id,
        OgreCutter.metadata.id,
        OneHundredEightPoundPhoenix.metadata.id,
      )
    ),
    CharacterMetadata(
      name = "Sinon",
      attackType = AttackType.Ranged,
      initialHealthPoints = 43,
      initialAttackPoints = 20,
      initialBasicAttackRange = 8,
      initialSpeed = 4,
      initialPsychicalDefense = 5,
      initialMagicalDefense = 5,
      initialAbilitiesMetadataIds = Seq(
        SnipersSight.metadata.id,
        TacticalEscape.metadata.id,
        PreciseShot.metadata.id,
      )
    ),
    CharacterMetadata(
      name = "Yasaka Mahiro",
      attackType = AttackType.Ranged,
      initialHealthPoints = 48,
      initialAttackPoints = 17,
      initialBasicAttackRange = 6,
      initialSpeed = 6,
      initialPsychicalDefense = 15,
      initialMagicalDefense = 15,
      initialAbilitiesMetadataIds = Seq.empty
    ),
    CharacterMetadata(
      name = "Crona",
      attackType = AttackType.Melee,
      initialHealthPoints = 78,
      initialAttackPoints = 9,
      initialBasicAttackRange = 3,
      initialSpeed = 6,
      initialPsychicalDefense = 25,
      initialMagicalDefense = 35,
      initialAbilitiesMetadataIds = Seq(
        BlackBlood.metadata.id,
        ScreechAlpha.metadata.id,
        Infection.metadata.id,
      )
    ),
    CharacterMetadata(
      name = "Itsuka Kotori",
      attackType = AttackType.Melee,
      initialHealthPoints = 55,
      initialAttackPoints = 16,
      initialBasicAttackRange = 5,
      initialSpeed = 5,
      initialPsychicalDefense = 15,
      initialMagicalDefense = 25,
      initialAbilitiesMetadataIds = Seq.empty
    ),
    CharacterMetadata(
      name = "『 』",
      attackType = AttackType.Melee,
      initialHealthPoints = 42,
      initialAttackPoints = 7,
      initialBasicAttackRange = 2,
      initialSpeed = 5,
      initialPsychicalDefense = 5,
      initialMagicalDefense = 5,
      initialAbilitiesMetadataIds = Seq(
        AceInTheHole.metadata.id,
        Check.metadata.id,
        Castling.metadata.id,
      )
    ),
    CharacterMetadata(
      name = "Shana",
      attackType = AttackType.Melee,
      initialHealthPoints = 62,
      initialAttackPoints = 15,
      initialBasicAttackRange = 2,
      initialSpeed = 7,
      initialPsychicalDefense = 15,
      initialMagicalDefense = 20,
      initialAbilitiesMetadataIds = Seq.empty
    ),
    CharacterMetadata(
      name = "Yoshino",
      attackType = AttackType.Melee,
      initialHealthPoints = 91,
      initialAttackPoints = 6,
      initialBasicAttackRange = 3,
      initialSpeed = 5,
      initialPsychicalDefense = 30,
      initialMagicalDefense = 45,
      initialAbilitiesMetadataIds = Seq.empty
    ),
    CharacterMetadata(
      name = "Kurogane Ikki",
      attackType = AttackType.Melee,
      initialHealthPoints = 63,
      initialAttackPoints = 12,
      initialBasicAttackRange = 3,
      initialSpeed = 6,
      initialPsychicalDefense = 20,
      initialMagicalDefense = 20,
      initialAbilitiesMetadataIds = Seq.empty
    ),
    CharacterMetadata(
      name = "Llenn",
      attackType = AttackType.Ranged,
      initialHealthPoints = 51,
      initialAttackPoints = 13,
      initialBasicAttackRange = 3,
      initialSpeed = 8,
      initialPsychicalDefense = 15,
      initialMagicalDefense = 10,
      initialAbilitiesMetadataIds = Seq(
        PChan.metadata.id,
        GrenadeThrow.metadata.id,
        RunItDown.metadata.id,
      )
    ),
    CharacterMetadata(
      name = "Kirito",
      attackType = AttackType.Melee,
      initialHealthPoints = 63,
      initialAttackPoints = 13,
      initialBasicAttackRange = 3,
      initialSpeed = 6,
      initialPsychicalDefense = 20,
      initialMagicalDefense = 15,
      initialAbilitiesMetadataIds = Seq.empty
    ),
    CharacterMetadata(
      name = "Monkey D. Luffy",
      attackType = AttackType.Melee,
      initialHealthPoints = 59,
      initialAttackPoints = 15,
      initialBasicAttackRange = 5,
      initialSpeed = 6,
      initialPsychicalDefense = 25,
      initialMagicalDefense = 15,
      initialAbilitiesMetadataIds = Seq.empty
    ),
    CharacterMetadata(
      name = "Nibutani Shinka",
      attackType = AttackType.Ranged,
      initialHealthPoints = 68,
      initialAttackPoints = 9,
      initialBasicAttackRange = 6,
      initialSpeed = 5,
      initialPsychicalDefense = 10,
      initialMagicalDefense = 20,
      initialAbilitiesMetadataIds = Seq.empty
    ),
    CharacterMetadata(
      name = "Sakai Yuuji",
      attackType = AttackType.Melee,
      initialHealthPoints = 70,
      initialAttackPoints = 20,
      initialBasicAttackRange = 3,
      initialSpeed = 5,
      initialPsychicalDefense = 15,
      initialMagicalDefense = 20,
      initialAbilitiesMetadataIds = Seq.empty
    ),
    CharacterMetadata(
      name = "Carmel Wilhelmina",
      attackType = AttackType.Ranged,
      initialHealthPoints = 87,
      initialAttackPoints = 10,
      initialBasicAttackRange = 5,
      initialSpeed = 5,
      initialPsychicalDefense = 35,
      initialMagicalDefense = 40,
      initialAbilitiesMetadataIds = Seq(
        ManipulatorOfObjects.metadata.id,
        BindingRibbons.metadata.id,
        TiamatsIntervention.metadata.id,
      )
    ),
    CharacterMetadata(
      name = "Ryuko Matoi",
      attackType = AttackType.Melee,
      initialHealthPoints = 67,
      initialAttackPoints = 14,
      initialBasicAttackRange = 4,
      initialSpeed = 6,
      initialPsychicalDefense = 15,
      initialMagicalDefense = 10,
      initialAbilitiesMetadataIds = Seq.empty
    ),
    CharacterMetadata(
      name = "Liones Elizabeth",
      attackType = AttackType.Melee,
      initialHealthPoints = 63,
      initialAttackPoints = 4,
      initialBasicAttackRange = 2,
      initialSpeed = 5,
      initialPsychicalDefense = 15,
      initialMagicalDefense = 15,
      initialAbilitiesMetadataIds = Seq.empty
    ),
    CharacterMetadata(
      name = "Satou Kazuma",
      attackType = AttackType.Melee,
      initialHealthPoints = 66,
      initialAttackPoints = 16,
      initialBasicAttackRange = 3,
      initialSpeed = 7,
      initialPsychicalDefense = 15,
      initialMagicalDefense = 15,
      initialAbilitiesMetadataIds = Seq.empty
    ),
    CharacterMetadata(
      name = "Derieri",
      attackType = AttackType.Melee,
      initialHealthPoints = 61,
      initialAttackPoints = 12,
      initialBasicAttackRange = 2,
      initialSpeed = 6,
      initialPsychicalDefense = 25,
      initialMagicalDefense = 25,
      initialAbilitiesMetadataIds = Seq.empty
    ),
    CharacterMetadata(
      name = "Ochaco Uraraka",
      attackType = AttackType.Melee,
      initialHealthPoints = 62,
      initialAttackPoints = 11,
      initialBasicAttackRange = 2,
      initialSpeed = 6,
      initialPsychicalDefense = 15,
      initialMagicalDefense = 15,
      initialAbilitiesMetadataIds = Seq.empty
    ),
    CharacterMetadata(
      name = "Levi",
      attackType = AttackType.Melee,
      initialHealthPoints = 57,
      initialAttackPoints = 23,
      initialBasicAttackRange = 3,
      initialSpeed = 7,
      initialPsychicalDefense = 10,
      initialMagicalDefense = 10,
      initialAbilitiesMetadataIds = Seq.empty
    ),
    CharacterMetadata(
      name = "Ononoki Yotsugi",
      attackType = AttackType.Melee,
      initialHealthPoints = 75,
      initialAttackPoints = 9,
      initialBasicAttackRange = 3,
      initialSpeed = 6,
      initialPsychicalDefense = 10,
      initialMagicalDefense = 25,
      initialAbilitiesMetadataIds = Seq.empty
    ),
    CharacterMetadata(
      name = "Sabrac",
      attackType = AttackType.Melee,
      initialHealthPoints = 62,
      initialAttackPoints = 16,
      initialBasicAttackRange = 2,
      initialSpeed = 5,
      initialPsychicalDefense = 15,
      initialMagicalDefense = 25,
      initialAbilitiesMetadataIds = Seq.empty
    ),
    CharacterMetadata(
      name = "Akame",
      attackType = AttackType.Melee,
      initialHealthPoints = 67,
      initialAttackPoints = 13,
      initialBasicAttackRange = 3,
      initialSpeed = 7,
      initialPsychicalDefense = 20,
      initialMagicalDefense = 15,
      initialAbilitiesMetadataIds = Seq(
        Murasame.metadata.id,
        Eliminate.metadata.id,
        LittleWarHorn.metadata.id,
      )
    ),
  )

  def getBotMetadatas: Seq[CharacterMetadata] = 1 to 30 map (i => {
    CharacterMetadata(
      name = s"Bot$i",
      AttackType.Melee,
      initialHealthPoints = 50,
      initialAttackPoints = 5,
      initialBasicAttackRange = 3,
      initialSpeed = 5,
      initialPsychicalDefense = 10,
      initialMagicalDefense = 10,
      Seq.empty,
    )
  })
}
