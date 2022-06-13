package com.tosware.NKM.services

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import com.tosware.NKM.NKMTimeouts
import com.tosware.NKM.actors.NKMData
import com.tosware.NKM.models.game.{HexMap, NKMCharacterMetadata}

import scala.concurrent.Await

class NKMDataService(implicit system: ActorSystem) extends NKMTimeouts
{
  val nkmDataActor: ActorRef = system.actorOf(NKMData.props())

  def getHexMaps: List[HexMap] = {
    aw(nkmDataActor ? NKMData.GetHexMaps).asInstanceOf[List[HexMap]]
  }

  def getCharactersMetadata: Seq[NKMCharacterMetadata] = 1 to 30 map (i => {
    NKMCharacterMetadata(
      name = s"Bot$i",
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
