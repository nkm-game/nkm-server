package com.tosware.NKM.actors

import akka.actor.{Actor, ActorLogging, Props}
import slick.jdbc.JdbcBackend.Database

object CQRSEventHandler {
  def props(): Props = Props(new CQRSEventHandler())
}

class CQRSEventHandler extends Actor with ActorLogging {
  override def preStart(): Unit = {
    log.info("CQRSEventHandler started")
    context.system.eventStream.subscribe(self, classOf[User.Event])
//    context.system.eventStream.subscribe(self, classOf[Game.Event])
  }
  override def receive = {
    case registerSuccess @ User.RegisterSuccess(email, passwordHash) =>
      val db = Database.forConfig("slick.db")
      log.info("Register received in CQRS Event Handler")
      log.info(registerSuccess.toString)
      // TODO insert user to relational db


  }
}
