## Architecture
### Actor system
#### Why actor system?
Actor system was used in order to use Event Sourcing persistable data.
A pure Event Sourcing framework would be better, but we have other priorities now than rewriting related code.
#### Persistable actors
Persistable actors have state that is derived from events that are written to the database.

Currently, we have 5 persistable actors:
1. User
2. Lobby
3. Game
4. Bug report
5. Game ID tracker

#### Session actors
Session actors listen to some events and track who is observing them.
After they receive an event, they send data to observers.
Data may be filtered and sent only to observers who have access to it.

We have Game and Lobby session actors.

#### Game ID tracker actor
When a new lobby is created, a new gameId is tracked in Game ID tracker.
This actor makes sure that there are no duplicates of a specific Lobby / Game actor.
