case class Stat(value: Int)

//object NKMCharacter {
//  def apply(name: String,
//            healthPoints: Int,
//            attackPoints: Int,
//            basicAttackRange: Int,
//            speed: Int,
//            psychicalDefense: Int,
//            magicalDefense: Int) =
//    new NKMCharacter(name,
//      healthPoints,
//      Stat(attackPoints),
//      Stat(basicAttackRange),
//      Stat(speed),
//      Stat(psychicalDefense),
//      Stat(magicalDefense))
//}

case class NKMCharacter(name: String,
                        healthPoints: Int,
                        attackPoints: Stat,
                        basicAttackRange: Stat,
                        speed: Stat,
                        psychicalDefense: Stat,
                        magicalDefense: Stat)
