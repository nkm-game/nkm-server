package com.tosware.NKM.serializers

import com.tosware.NKM.actors.Game
import com.tosware.NKM.actors.Game._
import com.tosware.NKM.actors.Lobby._
import com.tosware.NKM.models._
import com.tosware.NKM.models.lobby._
import spray.json._

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Try

trait NKMJsonProtocol extends DefaultJsonProtocol {

  implicit object HexCellEffectJsonFormat extends RootJsonFormat[HexCellEffect] {
    override def write(obj: HexCellEffect): JsValue = obj match {
      case _ => deserializationError("Not implemented yet")
    }
    override def read(json: JsValue): HexCellEffect = json match {
      case _ => deserializationError("Cannot deserialize abstract class")
    }
  }

  implicit object HexCellTypeJsonFormat extends RootJsonFormat[HexCellType] {
    override def write(obj: HexCellType): JsValue = obj match {
      case Transparent => JsString("Transparent")
      case Normal => JsString("Normal")
      case Wall => JsString("Wall")
      case SpawnPoint => JsString("SpawnPoint")
    }
    override def read(json: JsValue): HexCellType = json match {
      case JsString(value) => value match {
        case "Transparent" => Transparent
        case "Normal" => Normal
        case "Wall" => Wall
        case "SpawnPoint" => SpawnPoint
        case _ => deserializationError(s"Unable to parse:\n$value")
      }
      case _ => deserializationError(s"Unable to parse:\n$json")
    }
  }

  // borrowed from https://github.com/theiterators/kebs/blob/master/examples/src/main/scala/pl/iterators/kebs_examples/SprayJsonWithAkkaHttpExample.scala
  implicit object LocalDateFormat extends RootJsonFormat[LocalDate] {
    override def write(obj: LocalDate) = JsString(formatter.format(obj))

    override def read(json: JsValue) = json match {
      case JsString(lDString) =>
        Try(LocalDate.parse(lDString, formatter)).getOrElse(deserializationError(deserializationErrorMessage))
      case _ => deserializationError(deserializationErrorMessage)
    }

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val deserializationErrorMessage =
      s"Expected date time in ISO offset date time format ex. ${LocalDate.now().format(formatter)}"
  }

  // Start with simple ones, finish with the most complex
  // if format A depends on B, then B should be defined first (or we get a NullPointerException from spray)
  implicit val lobbyStateFormat: RootJsonFormat[LobbyState] = jsonFormat6(LobbyState)
  implicit val hexCoordinatesFormat: RootJsonFormat[HexCoordinates] = jsonFormat2(HexCoordinates)
  implicit val statFormat: RootJsonFormat[Stat] = jsonFormat1(Stat)
  implicit val phaseFormat: RootJsonFormat[Phase] = jsonFormat1(Phase)
  implicit val turnFormat: RootJsonFormat[Turn] = jsonFormat1(Turn)
  implicit val nkmCharacterFormat: RootJsonFormat[NKMCharacter] = jsonFormat8(NKMCharacter)
  implicit val playerFormat: RootJsonFormat[Player] = jsonFormat2(Player)
  implicit val hexCellFormat: RootJsonFormat[HexCell] = jsonFormat5(HexCell)
  implicit val hexMapFormat: RootJsonFormat[HexMap] = jsonFormat2(HexMap)
  implicit val gameStateFormat: RootJsonFormat[GameState] = jsonFormat5(GameState.apply)

  implicit val loginFormat: RootJsonFormat[Credentials] = jsonFormat2(Credentials)
  implicit val registerRequestFormat: RootJsonFormat[RegisterRequest] = jsonFormat3(RegisterRequest)
  implicit val lobbyCreationRequestFormat: RootJsonFormat[LobbyCreationRequest] = jsonFormat1(LobbyCreationRequest)
  implicit val lobbyJoinRequestFormat: RootJsonFormat[LobbyJoinRequest] = jsonFormat1(LobbyJoinRequest)
  implicit val lobbyLeaveRequestFormat: RootJsonFormat[LobbyLeaveRequest] = jsonFormat1(LobbyLeaveRequest)

  implicit object EventJsonFormat extends RootJsonFormat[Game.Event] {
    // Events
    implicit val playersSetFormat: RootJsonFormat[PlayersSet] = jsonFormat1(PlayersSet)
    implicit val characterAddedFormat: RootJsonFormat[CharacterAdded] = jsonFormat2(CharacterAdded)
    implicit val characterPlacedFormat: RootJsonFormat[CharacterPlaced] = jsonFormat2(CharacterPlaced)
    implicit val characterMovedFormat: RootJsonFormat[CharacterMoved] = jsonFormat2(CharacterMoved)
//    implicit val mapSetFormat: RootJsonFormat[MapSet] = jsonFormat1(MapSet)

    val playersSetId: JsString = JsString("PlayersSet")
    val characterAddedId: JsString = JsString("CharacterAdded")
    val characterPlacedId: JsString = JsString("CharacterPlaced")
    val characterMovedId: JsString = JsString("CharacterMoved")
    val mapSetId: JsString = JsString("MapSet")

    override def write(obj: Game.Event): JsValue = obj match {
      case e: PlayersSet => JsArray(Vector(playersSetId, playersSetFormat.write(e)))
      case e: CharacterAdded => JsArray(Vector(characterAddedId, characterAddedFormat.write(e)))
      case e: CharacterPlaced => JsArray(Vector(characterPlacedId, characterPlacedFormat.write(e)))
      case e: CharacterMoved => JsArray(Vector(characterMovedId, characterMovedFormat.write(e)))
//      case e: MapSet => JsArray(Vector(mapSetId, mapSetFormat.write(e)))
    }
    override def read(json: JsValue): Game.Event = json match {
      case JsArray(Vector(`playersSetId`, jsEvent)) => playersSetFormat.read(jsEvent)
      case JsArray(Vector(`characterAddedId`, jsEvent)) => characterAddedFormat.read(jsEvent)
      case JsArray(Vector(`characterPlacedId`, jsEvent)) => characterPlacedFormat.read(jsEvent)
      case JsArray(Vector(`characterMovedId`, jsEvent)) => characterMovedFormat.read(jsEvent)
//      case JsArray(Vector(`mapSetId`, jsEvent)) => mapSetFormat.read(jsEvent)
    }
  }


}
