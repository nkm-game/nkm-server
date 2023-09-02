## Adding new actor

### Persistent actors
Add serializer for events in `akka.conf` at `akka.actor.serialization-bindings`:

```scala
"com.tosware.nkm.actors.NewActor$Event" = kryo
```
