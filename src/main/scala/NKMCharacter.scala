import akka.actor.{Actor, ActorLogging, Props}

sealed trait StatType
case object HealthPoints extends StatType
case object AttackPoints extends StatType
case object Speed extends StatType
case object PhysicalDefense extends StatType
case object MagicalDefense extends StatType
case object BasicAttackRange extends StatType

case class Stat(statType: StatType, value: Int)

object NKMCharacter {
  case class GetStat(statType: StatType)
  case object GetName
  def props(name: String,
            healthPoints: Int,
            attackPoints: Int,
            basicAttackRange: Int,
            speed: Int,
            psychicalDefense: Int,
            magicalDefense: Int): Props =
    Props(new NKMCharacter(name,
      Stat(HealthPoints, healthPoints),
      Stat(AttackPoints, attackPoints),
      Stat(BasicAttackRange, basicAttackRange),
      Stat(Speed, speed),
      Stat(PhysicalDefense, psychicalDefense),
      Stat(MagicalDefense, magicalDefense)))
}

case class NKMCharacter(name: String
                        , healthPoints: Stat
                        , attackPoints: Stat
                        , basicAttackRange: Stat
                        , speed: Stat
                        , psychicalDefense: Stat
                        , magicalDefense: Stat
                       ) {

  def getStatByType(statType: StatType): Stat = statType match {
      case HealthPoints => healthPoints
      case AttackPoints => attackPoints
      case Speed => speed
      case PhysicalDefense => psychicalDefense
      case MagicalDefense => magicalDefense
      case BasicAttackRange => basicAttackRange
    }

//  import NKMCharacter._
//  override def receive: Receive = {
//    case GetName => {
//      log.info(s"Returning name: $name")
//      sender() ! name
//    }
//    case GetStat(statType) => {
//      val stat = getStatByType(statType)
//      log.info(s"Returning stat of type $statType: $stat")
//      sender() ! stat
//    }
//  }
}
