name := "NKMActorServer"

version := "0.1"

scalaVersion := "2.13.3"

lazy val AkkaVersion = "2.6.8"
lazy val AkkaHttpVersion = "10.2.3"
lazy val CassandraVersion = "1.0.1"
lazy val QuickLensVersion = "1.6.0"
lazy val LevelDBVersion = "1.8"
lazy val ScalaTestVersion = "3.2.0"
lazy val JwtVersion = "4.2.0"
lazy val ScalaBcryptVersion = "4.1"
lazy val LogbackClassicVersion = "1.2.3"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % AkkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % AkkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion

libraryDependencies += "com.pauldijou" %% "jwt-core" % JwtVersion
libraryDependencies += "com.pauldijou" %% "jwt-spray-json" % JwtVersion

//persistence
libraryDependencies += "com.typesafe.akka" %% "akka-persistence" % AkkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-persistence-query" % AkkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-cluster-tools" % AkkaVersion

//libraryDependencies += "com.typesafe.akka" %% "akka-persistence-cassandra" % CassandraVersion

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

scalacOptions in Compile ++= Seq("-deprecation", "-feature", "-unchecked")

