## Adding new actor

Add actor instance to `NkmDependencies.scala`

Remember to add the actor to `def cleanup():` there, so it will be cleaned up in between of tests.

### Persistent actors
Add serializer for events in `akka.conf` at `akka.actor.serialization-bindings`:

```scala
"com.tosware.nkm.actors.NewActor$Event" = kryo
```

