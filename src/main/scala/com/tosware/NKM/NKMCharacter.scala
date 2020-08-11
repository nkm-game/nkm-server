package com.tosware.NKM

case class Stat(value: Int)

//object com.tosware.NKM.NKMCharacter {
//  def apply(name: String,
//            healthPoints: Int,
//            attackPoints: Int,
//            basicAttackRange: Int,
//            speed: Int,
//            psychicalDefense: Int,
//            magicalDefense: Int) =
//    new com.tosware.NKM.NKMCharacter(name,
//      healthPoints,
//      com.tosware.NKM.Stat(attackPoints),
//      com.tosware.NKM.Stat(basicAttackRange),
//      com.tosware.NKM.Stat(speed),
//      com.tosware.NKM.Stat(psychicalDefense),
//      com.tosware.NKM.Stat(magicalDefense))
//}

case class NKMCharacter(name: String,
                        healthPoints: Int,
                        attackPoints: Stat,
                        basicAttackRange: Stat,
                        speed: Stat,
                        psychicalDefense: Stat,
                        magicalDefense: Stat)
