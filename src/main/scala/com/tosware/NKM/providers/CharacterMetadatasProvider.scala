package com.tosware.NKM.providers

import com.tosware.NKM.models.game.abilities.aqua._
import com.tosware.NKM.models.game.{AttackType, NKMCharacterMetadata}
import com.tosware.NKM.serializers.NKMJsonProtocol

case class CharacterMetadatasProvider() extends NKMJsonProtocol {

  def getCharacterMetadatas: Seq[NKMCharacterMetadata] = Seq(
    NKMCharacterMetadata(
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
    NKMCharacterMetadata(
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
    NKMCharacterMetadata(
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
    NKMCharacterMetadata(
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
    NKMCharacterMetadata(
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
    NKMCharacterMetadata(
      name = "Hecate",
      attackType = AttackType.Ranged,
      initialHealthPoints = 59,
      initialAttackPoints = 11,
      initialBasicAttackRange = 7,
      initialSpeed = 3,
      initialPsychicalDefense = 5,
      initialMagicalDefense = 20,
      initialAbilitiesMetadataIds = Seq.empty
    ),
    NKMCharacterMetadata(
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
    NKMCharacterMetadata(
      name = "Roronoa Zoro",
      attackType = AttackType.Melee,
      initialHealthPoints = 87,
      initialAttackPoints = 27,
      initialBasicAttackRange = 2,
      initialSpeed = 8,
      initialPsychicalDefense = 40,
      initialMagicalDefense = 25,
      initialAbilitiesMetadataIds = Seq.empty
    ),
    NKMCharacterMetadata(
      name = "Sinon",
      attackType = AttackType.Ranged,
      initialHealthPoints = 43,
      initialAttackPoints = 20,
      initialBasicAttackRange = 8,
      initialSpeed = 4,
      initialPsychicalDefense = 5,
      initialMagicalDefense = 5,
      initialAbilitiesMetadataIds = Seq.empty
    ),
    NKMCharacterMetadata(
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
    NKMCharacterMetadata(
      name = "Crona",
      attackType = AttackType.Melee,
      initialHealthPoints = 78,
      initialAttackPoints = 9,
      initialBasicAttackRange = 3,
      initialSpeed = 6,
      initialPsychicalDefense = 25,
      initialMagicalDefense = 35,
      initialAbilitiesMetadataIds = Seq.empty
    ),
    NKMCharacterMetadata(
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
    NKMCharacterMetadata(
      name = "『 』",
      attackType = AttackType.Melee,
      initialHealthPoints = 42,
      initialAttackPoints = 7,
      initialBasicAttackRange = 2,
      initialSpeed = 5,
      initialPsychicalDefense = 5,
      initialMagicalDefense = 5,
      initialAbilitiesMetadataIds = Seq.empty
    ),
    NKMCharacterMetadata(
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
    NKMCharacterMetadata(
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
    NKMCharacterMetadata(
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
    NKMCharacterMetadata(
      name = "Llenn",
      attackType = AttackType.Ranged,
      initialHealthPoints = 51,
      initialAttackPoints = 13,
      initialBasicAttackRange = 3,
      initialSpeed = 8,
      initialPsychicalDefense = 15,
      initialMagicalDefense = 10,
      initialAbilitiesMetadataIds = Seq.empty
    ),
    NKMCharacterMetadata(
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
    NKMCharacterMetadata(
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
    NKMCharacterMetadata(
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
    NKMCharacterMetadata(
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
    NKMCharacterMetadata(
      name = "Carmel Wilhelmina",
      attackType = AttackType.Ranged,
      initialHealthPoints = 87,
      initialAttackPoints = 10,
      initialBasicAttackRange = 5,
      initialSpeed = 5,
      initialPsychicalDefense = 35,
      initialMagicalDefense = 40,
      initialAbilitiesMetadataIds = Seq.empty
    ),
    NKMCharacterMetadata(
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
    NKMCharacterMetadata(
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
    NKMCharacterMetadata(
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
    NKMCharacterMetadata(
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
    NKMCharacterMetadata(
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
    NKMCharacterMetadata(
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
    NKMCharacterMetadata(
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
    NKMCharacterMetadata(
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
    NKMCharacterMetadata(
      name = "Akame",
      attackType = AttackType.Melee,
      initialHealthPoints = 67,
      initialAttackPoints = 13,
      initialBasicAttackRange = 3,
      initialSpeed = 7,
      initialPsychicalDefense = 20,
      initialMagicalDefense = 15,
      initialAbilitiesMetadataIds = Seq.empty
    ),
  )

  def getBotMetadatas: Seq[NKMCharacterMetadata] = 1 to 30 map (i => {
    NKMCharacterMetadata(
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
