package NKM

case class Stat(value: Int)

//object NKM.NKMCharacter {
//  def apply(name: String,
//            healthPoints: Int,
//            attackPoints: Int,
//            basicAttackRange: Int,
//            speed: Int,
//            psychicalDefense: Int,
//            magicalDefense: Int) =
//    new NKM.NKMCharacter(name,
//      healthPoints,
//      NKM.Stat(attackPoints),
//      NKM.Stat(basicAttackRange),
//      NKM.Stat(speed),
//      NKM.Stat(psychicalDefense),
//      NKM.Stat(magicalDefense))
//}

case class NKMCharacter(name: String,
                        healthPoints: Int,
                        attackPoints: Stat,
                        basicAttackRange: Stat,
                        speed: Stat,
                        psychicalDefense: Stat,
                        magicalDefense: Stat)
