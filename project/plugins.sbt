addSbtPlugin("au.com.onegeek" % "sbt-dotenv" % "2.1.204")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.4")
// TODO: run `migrate-libs nkmactorserver` and check if al libraries are safe to run in Scala 3
addSbtPlugin("ch.epfl.scala" % "sbt-scala3-migrate" % "0.4.6")