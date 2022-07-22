package integration.services

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.persistence.jdbc.query.scaladsl.JdbcReadJournal
import akka.persistence.query.PersistenceQuery
import akka.stream.scaladsl.Sink
import com.tosware.NKM.actors.User
import com.tosware.NKM.actors.User._
import helpers.NKMPersistenceTestKit

import scala.language.postfixOps

class GameServiceSpec extends NKMPersistenceTestKit(ActorSystem("GameServiceSpec")) {
//  val gameService = deps.gameService

  "GameService" must {
    "allow moving characters" in {
      // disallow if character is grounded

      // disallow if character is snared

      // disallow if character is stunned

      // disallow empty move

      // allow move within speed range

      // disallow move above speed range

      // disallow move into the same position

      // disallow move that visits another cell more than once

      // disallow move if character already moved

      // disallow move if there is an obstacle on path

      // disallow move if cell at the end is not free to move
      fail()
    }
  }
}
