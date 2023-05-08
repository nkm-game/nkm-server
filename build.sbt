name := "nkm-actor-server"

version := "0.2"

scalaVersion := "2.13.10"

enablePlugins(JavaAppPackaging)

// needed because of issues like in https://stackoverflow.com/questions/19425613/unsatisfiedlinkerror-with-native-library-under-sbt
fork := true

lazy val AkkaVersion = "2.6.19"
lazy val AkkaHttpVersion = "10.2.9"
lazy val AkkaPersistenceVersion = "3.5.3"
lazy val CassandraVersion = "1.0.1"
lazy val QuickLensVersion = "1.8.8"
lazy val LevelDBVersion = "1.8"
lazy val ScalaTestVersion = "3.2.12"
lazy val JwtVersion = "5.0.0"
lazy val ScalaBcryptVersion = "4.3.0"
lazy val LogbackClassicVersion = "1.2.11"
lazy val KebsVersion = "1.9.4"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % AkkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % AkkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion
libraryDependencies += "io.altoo" %% "akka-kryo-serialization" % "2.4.3"

libraryDependencies += "pl.iterators" %% "kebs-spray-json" % KebsVersion
libraryDependencies += "pl.iterators" %% "kebs-slick" % KebsVersion
//libraryDependencies += "pl.iterators" %% "kebs-akka-http" % KebsVersion

libraryDependencies += "com.beachape" %% "enumeratum" % "1.7.0"


libraryDependencies += "com.pauldijou" %% "jwt-core" % JwtVersion
libraryDependencies += "com.pauldijou" %% "jwt-spray-json" % JwtVersion

//persistence
libraryDependencies += "com.typesafe.akka" %% "akka-persistence" % AkkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-persistence-query" % AkkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-cluster-tools" % AkkaVersion

libraryDependencies += "com.softwaremill.quicklens" %% "quicklens" % QuickLensVersion

libraryDependencies += "org.fusesource.leveldbjni" % "leveldbjni-all" % LevelDBVersion

// password hash
libraryDependencies += "com.github.t3hnar" %% "scala-bcrypt" % ScalaBcryptVersion

// logging
libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion
libraryDependencies += "ch.qos.logback" % "logback-classic" % LogbackClassicVersion

// testing
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % AkkaVersion % Test
libraryDependencies += "com.typesafe.akka" %% "akka-persistence-testkit" % AkkaVersion % Test
libraryDependencies += "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion % Test
libraryDependencies += "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion % Test
libraryDependencies += "org.scalatest" %% "scalatest" % ScalaTestVersion % Test

libraryDependencies += "com.github.dnvriend" %% "akka-persistence-inmemory" % "2.5.15.2"

libraryDependencies += "com.github.dnvriend" %% "akka-persistence-jdbc" % AkkaPersistenceVersion
libraryDependencies += "mysql" % "mysql-connector-java" % "8.0.30"

Compile / scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Ywarn-unused:imports",
  "-Xfatal-warnings",
  "-Xsource:3",
)