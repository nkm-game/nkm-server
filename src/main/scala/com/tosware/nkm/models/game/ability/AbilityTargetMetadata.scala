package com.tosware.nkm.models.game.ability

object AbilityTargetMetadata {
  val SingleCharacter = AbilityTargetMetadata(1 to 1, AbilityTargetType.Character)
  val SingleCoordinate = AbilityTargetMetadata(1 to 1, AbilityTargetType.HexCoordinates)
  val CircularAirSelection = AbilityTargetMetadata(1 to 1, AbilityTargetType.HexCoordinates, AirSelectionType.Circular)
}

final case class AbilityTargetMetadata(
    allowedRange: Range,
    targetType: AbilityTargetType,
    airSelectionType: AirSelectionType = AirSelectionType.None,
) {
  def toMarshallable =
    AbilityTargetMetadataMarshallable(
      allowedRange.min,
      allowedRange.max,
      targetType,
      airSelectionType,
    )
}

final case class AbilityTargetMetadataMarshallable(
    allowedFrom: Int,
    allowedTo: Int,
    targetType: AbilityTargetType,
    airSelectionType: AirSelectionType = AirSelectionType.None,
)
