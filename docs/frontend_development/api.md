## Abilities
### Ability targets
An ability can have:
- a character target
- a hex coordinate target
- a combination of the above
- no targets
### Collecting and sending ability targets
Depending on ability, a different UI should be displayed to collect and send the ability targets.

We need to think of a better way to do this.

### Frontend ability display brainstorm
#### Aqua
1. Purification: highlight characters from targets in range and allow to select one.
2. Resurrection: highlight or display dead characters that can be resurrected (should be in ability state) and allow to select one.
Then highlight and zoom into spawn points to choose from and allow to select one.
#### Ayatsuji Ayase
1. MarkOfTheWind: highlight cells in range and allow to select one.
2. CrackTheSky: highlight traps and allow to select one or more to detonate. (order matters)
#### Blank
1. Check: highlight characters from targets in range and allow to select one.
2. Castling: highlight all characters and force user to select two distinct characters.
#### Carmel Wilhelmina
1. BindingRibbons: show circular air selection in range and send middle cell to the server.
2. TiamatsIntervention: highlight character in range and allow to select one, then highlight cells in range and allow to select one.
Send selected character and cell to the server.
#### Dekomori Sanae
1. MjolnirHammer: show characters in range, select one and send to the server.
Then show characters in range again and send to the server again.
2. MjolnirDestinyImpulse: show circular air selection in range and send middle cell to the server.
Then immediately show it again if this ability is free because of a kill.

### Conclusion
We should improve ability metadata to have more data:
1. airSelectionType (enum of `None`, `Circular`, `EtcIfNewAdded`)
2. abilityTargets (`Seq(AbilityTarget)`)
    ```scala
    case class AbilityTarget(
      numberRange: Range,
      ttype: TargetType,
      selectionType: TargetSelectionType = TargetSelectionType.Map
    )
    ```
   Examples:
    1. Purification: `Seq((1 to 1, Target.Character))`
    2. Resurrection: `Seq((1 to 1, Target.Character, TargetSelectionType.DeadCharacters), (1 to 1, Target.HexCoordinate))`
    3. MarkOfTheWind: `Seq((1 to 1, Target.HexCoordinate))`
    4. CrackTheSky: `Seq((1 to ∞, Target.HexCoordinate))`
    5. Check: `Seq((1 to 1, Target.Character))`
    6. Castling: `Seq((2 to 2, Target.Character))`
    7. BindingRibbons: `Seq((1 to 1, Target.HexCoordinate))`
    8. TiamatsIntervention: `Seq((1 to 1, Target.Character), (1 to 1, Target.HexCoordinate))`
    9. MjolnirHammer: `Seq((1 to 1, Target.Character))`
    10. MjolnirDestinyImpulse: `Seq((1 to 1, Target.HexCoordinate))`
