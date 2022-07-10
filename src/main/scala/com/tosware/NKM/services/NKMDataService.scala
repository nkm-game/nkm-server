package com.tosware.NKM.services

import akka.actor.ActorSystem
import com.tosware.NKM.{HexMapProvider, NKMTimeouts}
import com.tosware.NKM.models.game.{HexMap, NKMCharacterMetadata}

class NKMDataService(implicit system: ActorSystem) extends NKMTimeouts
{

  def getHexMaps: Seq[HexMap] = HexMapProvider().getHexMaps

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
