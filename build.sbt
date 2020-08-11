name := "NKMActorServer"

version := "0.1"

scalaVersion := "2.13.3"

lazy val AkkaVersion = "2.6.8"
lazy val AkkaHttpVersion = "10.2.0"
lazy val CassandraVersion = "1.0.1"
lazy val QuickLensVersion = "1.6.0"
lazy val LevelDBVersion = "1.8"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % AkkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % AkkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion

libraryDependencies += "com.typesafe.akka" %% "akka-persistence" % AkkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-persistence-query" % AkkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-cluster-tools" % AkkaVersion

//libraryDependencies += "com.typesafe.akka" %% "akka-persistence-cassandra" % CassandraVersion

libraryDependencies += "com.softwaremill.quicklens" %% "quicklens" % QuickLensVersion

libraryDependencies += "org.fusesource.leveldbjni" % "leveldbjni-all" % LevelDBVersion
