package com.tosware.nkm.models.game.ability

import com.tosware.nkm.CharacterId
import com.tosware.nkm.models.game.hex.HexCoordinates
import com.tosware.nkm.serializers.NkmJsonProtocol
import spray.json.*
import NkmJsonProtocol.*

trait HexCoordinatesMarker

object UseData {
  implicit object HexCoordinatesMarker extends HexCoordinatesMarker

  def apply(characterId: CharacterId): UseData =
    UseData(Seq(characterId))
  def apply(hexCoordinates: HexCoordinates): UseData =
    UseData(Seq(hexCoordinates.toJson.toString))
  def apply(hexCoordinatesSeq: Seq[HexCoordinates])(implicit ev: HexCoordinatesMarker): UseData =
    UseData(hexCoordinatesSeq.map(_.toJson.toString))
}

final case class UseData(data: Seq[String] = Seq.empty) {
  def getCharacterIdAt(index: Int): CharacterId = data(index)
  def getCoordinatesAt(index: Int): HexCoordinates = data(index).parseJson.convertTo[HexCoordinates]

  def firstAsCharacterId: CharacterId = getCharacterIdAt(0)
  def firstAsCoordinates: HexCoordinates = getCoordinatesAt(0)

  def secondAsCharacterId: CharacterId = getCharacterIdAt(1)
  def secondAsCoordinates: HexCoordinates = getCoordinatesAt(1)

  def allAsCharacterIds: Seq[CharacterId] = data
  def allAsCoordinates: Seq[HexCoordinates] = data.map(_.parseJson.convertTo[HexCoordinates])
}
