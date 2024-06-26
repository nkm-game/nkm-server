## Adding new test hexmap

1. Add the map name to `TestHexMapName` enum.
2. Add another map to `com.tosware.nkm.models.game.hex.testmap` (remember to use the same name as in point 1.).
3. Add hexmap reference to `com.tosware.nkm.providers.HexMapProvider` method `getTestHexMaps`
4. Done, now you can use it in a test scenario like:
```scala
private val s = TestScenario.generate(TestHexMapName.YourNewMapName)
```