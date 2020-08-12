package com.tosware.NKM.models

case class Stat(value: Int)

//object com.tosware.NKM.models.NKMCharacter {
//  def apply(name: String,
//            healthPoints: Int,
//            attackPoints: Int,
//            basicAttackRange: Int,
//            speed: Int,
//            psychicalDefense: Int,
//            magicalDefense: Int) =
//    new com.tosware.NKM.models.NKMCharacter(name,
//      healthPoints,
//      com.tosware.NKM.models.Stat(attackPoints),
//      com.tosware.NKM.models.Stat(basicAttackRange),
//      com.tosware.NKM.models.Stat(speed),
//      com.tosware.NKM.models.Stat(psychicalDefense),
//      com.tosware.NKM.models.Stat(magicalDefense))
//}

case class NKMCharacter(name: String,
                        healthPoints: Int,
                        attackPoints: Stat,
                        basicAttackRange: Stat,
                        speed: Stat,
                        psychicalDefense: Stat,
                        magicalDefense: Stat)
