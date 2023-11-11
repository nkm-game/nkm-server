package com.tosware.nkm.serializers

import com.tosware.nkm.*
import com.tosware.nkm.models.game.event.GameEvent.*
import com.tosware.nkm.models.game.hex.HexCoordinates
import pl.iterators.kebs.instances.time.{LocalDateTimeString, ZonedDateTimeString}
import pl.iterators.kebs.json.{KebsEnumFormats, KebsSpray}
import spray.json.*

final case class GameEventSerialized(className: String, eventJson: String)

trait NkmJsonProtocol
    extends DefaultJsonProtocol
    with KebsSpray
    with KebsEnumFormats // enumeratum support
    with KebsSpray.NoFlat // jwt serialize / deserialize does not work with flat serialization (idk why)
    with ZonedDateTimeString
    with LocalDateTimeString {
  implicit object CoordinatesCharacterMapFormat extends RootJsonFormat[Map[HexCoordinates, CharacterId]] {
    override def write(obj: Map[HexCoordinates, CharacterId]) = {
      val m: List[JsField] = obj.map { case (coordinates, characterId) =>
        (coordinates.toJson.toString(), JsString(characterId))
      }.toList
      JsObject(m*)
    }

    override def read(json: JsValue) = json match {
      case JsObject(fields) => fields.map { case (coordinates, characterId) =>
          (coordinates.parseJson.convertTo[HexCoordinates], characterId.convertTo[String])
        }
      case x => deserializationError(s"Expected object, but got $x.")
    }
  }

  implicit object GameEventFormat extends RootJsonFormat[GameEvent] {
    override def write(obj: GameEvent): JsValue =
      JsObject((obj match {
        case e: GameStatusUpdated          => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: EventsRevealed             => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: ClockUpdated               => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: CharacterPlaced            => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: EffectAddedToCell          => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: EffectRemovedFromCell      => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: EffectAddedToCharacter     => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: EffectRemovedFromCharacter => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: EffectVariableSet          => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: AbilityHitCharacter        => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: AbilityUsed                => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: AbilityUsedOnCoordinates   => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: AbilityUsedOnCharacter     => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: AbilityUseFinished         => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: AbilityVariableSet         => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: CharacterBasicMoved        => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: CharacterPreparedToAttack  => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: CharacterBasicAttacked     => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: CharacterTeleported        => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: DamagePrepared             => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: DamageAmplified            => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: DamageSent                 => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: ShieldDamaged              => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: CharacterDamaged           => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: HealPrepared               => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: HealAmplified              => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: CharacterHealed            => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: CharacterHpSet             => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: CharacterShieldSet         => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: CharacterAttackTypeSet     => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: CharacterStatSet           => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: CharacterDied              => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: CharacterRemovedFromMap    => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: CharacterTookAction        => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: BasicAttackRefreshed       => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: BasicMoveRefreshed         => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: AnythingRefreshed          => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: CharactersPicked           => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: PlayerLost                 => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: PlayerSurrendered          => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: PlayerBanned               => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: PlayerFinishedBanning      => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: PlayerPicked               => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: PlayerBlindPicked          => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: PlayerFinishedBlindPicking => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: BanningPhaseFinished       => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: PlacingCharactersFinished  => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: TurnFinished               => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: TurnStarted                => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case e: PhaseFinished              => GameEventSerialized(e.getClass.getSimpleName, e.toJson.toString).toJson
        case unknown                       => deserializationError(s"json deserialize error: $unknown")
      }).asJsObject.fields)

    override def read(json: JsValue): GameEvent = {
      val ges = json.convertTo[GameEventSerialized]
      ges.className match {
        case "GameStatusUpdated"          => ges.eventJson.parseJson.convertTo[GameStatusUpdated]
        case "EventsRevealed"             => ges.eventJson.parseJson.convertTo[EventsRevealed]
        case "ClockUpdated"               => ges.eventJson.parseJson.convertTo[ClockUpdated]
        case "CharacterPlaced"            => ges.eventJson.parseJson.convertTo[CharacterPlaced]
        case "EffectAddedToCell"          => ges.eventJson.parseJson.convertTo[EffectAddedToCell]
        case "EffectRemovedFromCell"      => ges.eventJson.parseJson.convertTo[EffectRemovedFromCell]
        case "EffectAddedToCharacter"     => ges.eventJson.parseJson.convertTo[EffectAddedToCharacter]
        case "EffectRemovedFromCharacter" => ges.eventJson.parseJson.convertTo[EffectRemovedFromCharacter]
        case "EffectVariableSet"          => ges.eventJson.parseJson.convertTo[EffectVariableSet]
        case "AbilityHitCharacter"        => ges.eventJson.parseJson.convertTo[AbilityHitCharacter]
        case "AbilityUsed"                => ges.eventJson.parseJson.convertTo[AbilityUsed]
        case "AbilityUsedOnCoordinates"   => ges.eventJson.parseJson.convertTo[AbilityUsedOnCoordinates]
        case "AbilityUsedOnCharacter"     => ges.eventJson.parseJson.convertTo[AbilityUsedOnCharacter]
        case "AbilityUseFinished"         => ges.eventJson.parseJson.convertTo[AbilityUseFinished]
        case "AbilityVariableSet"         => ges.eventJson.parseJson.convertTo[AbilityVariableSet]
        case "CharacterBasicMoved"        => ges.eventJson.parseJson.convertTo[CharacterBasicMoved]
        case "CharacterPreparedToAttack"  => ges.eventJson.parseJson.convertTo[CharacterPreparedToAttack]
        case "CharacterBasicAttacked"     => ges.eventJson.parseJson.convertTo[CharacterBasicAttacked]
        case "CharacterTeleported"        => ges.eventJson.parseJson.convertTo[CharacterTeleported]
        case "DamagePrepared"             => ges.eventJson.parseJson.convertTo[DamagePrepared]
        case "DamageAmplified"            => ges.eventJson.parseJson.convertTo[DamageAmplified]
        case "DamageSent"                 => ges.eventJson.parseJson.convertTo[DamageSent]
        case "ShieldDamaged"              => ges.eventJson.parseJson.convertTo[ShieldDamaged]
        case "CharacterDamaged"           => ges.eventJson.parseJson.convertTo[CharacterDamaged]
        case "HealPrepared"               => ges.eventJson.parseJson.convertTo[HealPrepared]
        case "HealAmplified"              => ges.eventJson.parseJson.convertTo[HealAmplified]
        case "CharacterHealed"            => ges.eventJson.parseJson.convertTo[CharacterHealed]
        case "CharacterHpSet"             => ges.eventJson.parseJson.convertTo[CharacterHpSet]
        case "CharacterShieldSet"         => ges.eventJson.parseJson.convertTo[CharacterShieldSet]
        case "CharacterAttackTypeSet"     => ges.eventJson.parseJson.convertTo[CharacterAttackTypeSet]
        case "CharacterStatSet"           => ges.eventJson.parseJson.convertTo[CharacterStatSet]
        case "CharacterDied"              => ges.eventJson.parseJson.convertTo[CharacterDied]
        case "CharacterRemovedFromMap"    => ges.eventJson.parseJson.convertTo[CharacterRemovedFromMap]
        case "CharacterTookAction"        => ges.eventJson.parseJson.convertTo[CharacterTookAction]
        case "BasicAttackRefreshed"       => ges.eventJson.parseJson.convertTo[BasicAttackRefreshed]
        case "BasicMoveRefreshed"         => ges.eventJson.parseJson.convertTo[BasicMoveRefreshed]
        case "AnythingRefreshed"          => ges.eventJson.parseJson.convertTo[AnythingRefreshed]
        case "CharactersPicked"           => ges.eventJson.parseJson.convertTo[CharactersPicked]
        case "PlayerLost"                 => ges.eventJson.parseJson.convertTo[PlayerLost]
        case "PlayerSurrendered"          => ges.eventJson.parseJson.convertTo[PlayerSurrendered]
        case "PlayerBanned"               => ges.eventJson.parseJson.convertTo[PlayerBanned]
        case "PlayerFinishedBanning"      => ges.eventJson.parseJson.convertTo[PlayerFinishedBanning]
        case "PlayerPicked"               => ges.eventJson.parseJson.convertTo[PlayerPicked]
        case "PlayerBlindPicked"          => ges.eventJson.parseJson.convertTo[PlayerBlindPicked]
        case "PlayerFinishedBlindPicking" => ges.eventJson.parseJson.convertTo[PlayerFinishedBlindPicking]
        case "BanningPhaseFinished"       => ges.eventJson.parseJson.convertTo[BanningPhaseFinished]
        case "PlacingCharactersFinished"  => ges.eventJson.parseJson.convertTo[PlacingCharactersFinished]
        case "TurnFinished"               => ges.eventJson.parseJson.convertTo[TurnFinished]
        case "TurnStarted"                => ges.eventJson.parseJson.convertTo[TurnStarted]
        case "PhaseFinished"              => ges.eventJson.parseJson.convertTo[PhaseFinished]
        case unrecognized                 => serializationError(s"json serialization error $unrecognized")
      }
    }
  }
}
