# How to play NKM
NKM is a turn based game on a hexagonal map.

You can fight with other players using characters that can move, basic attack and use abilities.

## Lobbies
To play the game, first you must create a lobby, where you can set the game settings:

- lobby name
- hex map
- number of bans
- number of characters per player
- pick type
- clock config

Other users can join your lobby.

Once all settings are set and players joined the lobby, the host (lobby creator) can start the game.

## Game Types
### Deathmatch
You win when other players are eliminated.

To eliminate a player you have to kill all of his characters.

The game may end in a draw for the last players if they kill each other.
### Capture the point
To be added.

## Pick Types
### Blind pick
You have to select required amount characters (number is defined in lobby settings).

Other players won't see what characters you picked until everyone finished picking.

Failing to do so in time will result in losing the game.
### Draft pick
You can ban as many characters as you want, up to the number defined in the lobby settings.

Other players won't see what characters you banned until everyone finished banning.

Then players will draft their characters one by one, in the snake order (the draft order will reverse after one round of picking).

Failing pick a character in time will result in losing the game.
### All random
Characters are randomly assigned to players and randomly placed.
## Surrendering
You can surrender at any point in the game you are still playing in.
Surrendering in champion select will result in you losing the game and aborting it, causing all other players to draw the game.
Surrendering after champion select eliminates you from the game.
## Hex map
A hex map is a group of hex cells related to each other.
A hex cell may have a character and hex cell effects on it.
### Hex cell types
- normal
- wall
- spawn point (assigned to a player)

## Game rules
After character pick phase, the real game begins.

Players place characters and then take turns one after another.

### Character placing
Players place all characters at once on their spawn.

### Turns
In a turn you have to make an action with character.

You may choose to not make an action by passing a turn with a selected character.

You can only make actions with the same character once in a phase.

If you don't have any character to make action with, your turn is skipped.

Available actions:

- basic move
- basic attack
- use ability

When a turn ends, cooldowns on abilities and effects on character that took action are decreased by one.
### Phases
When you finish a turn, a character that made action in this turn cannot take action in future turns till the end of the phase.

When every character alive takes action, a phase is finished and every character may take actions again.
### Character State
A character has a certain state containing:
- name
- attack type (melee or ranged that can attack over walls and enemies)
- max health points
- health points
- attack points
- basic attack range
- speed
- physical defense (decreases incoming physical damage by %)
- magical defense (decreases incoming magical damage by %)
- shield (decreases any incoming damage flat)
- abilities
- effects

When a character has no Health Points left, they are removed from the map.

#### Abilities
Types of abilities:

- passive
- normal
- ultimate

#### Effects
Types of effects:

- positive
- negative
- neutral
- mixed

#### Constraints
1. A character may use only one ability or basic attack per turn.
2. A character cannot move and use an ultimate ability in one turn.
3. A character cannot move if they are stunned, grounded or snared.
4. A character cannot basic attack if they are stunned or disarmed.
5. A character cannot use abilities if they are stunned or silenced.
6. Passive abilities cannot be used.
7. Abilities on cooldown cannot be used.
8. Abilities on characters outside map cannot be used.
9. Target character of ability has to be in range.
10. Some abilities may have additional use constraints.