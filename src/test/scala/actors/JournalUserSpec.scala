//package actors
//
//import akka.actor.{ActorRef, ActorSystem}
//import akka.pattern.ask
//import akka.persistence.inmemory.query.scaladsl.InMemoryReadJournal
//import akka.persistence.jdbc.query.scaladsl.JdbcReadJournal
//import akka.persistence.query.scaladsl._
//import akka.persistence.query.{EventEnvelope, PersistenceQuery}
//import akka.stream.ActorMaterializer
//import akka.stream.scaladsl.{Flow, Sink}
//import com.tosware.NKM.actors.User
//import com.tosware.NKM.actors.User._
//import helpers.NKMPersistenceTestKit
//
//import scala.concurrent.Await
//import scala.language.postfixOps
//
//class JournalUserSpec extends NKMPersistenceTestKit(ActorSystem("UserSpec2"))
//{
//  "An User actor" must {
//
//    // this test does not print anything, leaving it so I can investigate it some day
//    "be able to read registered journal" in {
//      val user: ActorRef = system.actorOf(User.props("test7"))
//      val registerFuture = user ? Register("test@example.com","password")
//      val response = Await.result(registerFuture.mapTo[RegisterEvent], atMost)
//      response shouldBe RegisterSuccess
//
////      val readJournal: ReadJournal with CurrentPersistenceIdsQuery with PersistenceIdsQuery with CurrentEventsByPersistenceIdQuery with EventsByPersistenceIdQuery with CurrentEventsByTagQuery with EventsByTagQuery = {
////        PersistenceQuery(system).readJournalFor(InMemoryReadJournal.Identifier)
////      }
//      val readJournal = PersistenceQuery(system).readJournalFor[JdbcReadJournal](JdbcReadJournal.Identifier)
//
//      val src = readJournal.eventsByPersistenceId("user-test7", 0L, Long.MaxValue)
////        val events = src.map(_.event)
//
////      val sink = Sink.foreach(println)
////      val flow = Flow[EventEnvelope].map(x => s"test ${x.sequenceNr}")
////      val graph = src.via(flow).to(sink)
////      graph.run()
//      implicit val mat: ActorMaterializer = ActorMaterializer()
//      readJournal.persistenceIds().runForeach { event =>
//        println("Event: " + event)
//      }
//      src.runForeach { event =>
//        println("Event: " + event)
//      }
//
//
////      readJournal
////        .eventsByPersistenceId("user-test7", 0L, Long.MaxValue)
////        .map(envelope => envelope.event)
////        .map(convertToReadSideTypes)
//
////        println(readJournal.persistenceIds())
////        println(events.asInstanceOf[src.Repr[User.Event]])
////        println(src.map(_.persistenceId))
////        println(src)
//    }
//  }
//}
