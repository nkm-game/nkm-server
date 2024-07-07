addSbtPlugin("au.com.onegeek" % "sbt-dotenv" % "2.1.204")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.16") // needed for `sbt stage` command in Dockerfile
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.9")
// TODO: run `sbt migrate-libs nkm-actor-server` and check if al libraries are safe to run in Scala 3
addSbtPlugin("ch.epfl.scala" % "sbt-scala3-migrate" % "0.6.1")
