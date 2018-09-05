name := "advertisement-kafka-streaming"

version := "0.1"

scalaVersion := "2.11.12"

scalacOptions += "-Ypartial-unification"
resolvers ++= Seq (
  Opts.resolver.mavenLocalFile,
  "Confluent" at "http://packages.confluent.io/maven",
  Resolver.sonatypeRepo("releases"),
  Opts.resolver.sonatypeSnapshots
)

sourceGenerators in Compile += (avroScalaGenerateSpecific in Compile).taskValue

val confluentVersion = "4.1.1"

libraryDependencies ++= Seq(
  "com.lightbend" %% "kafka-streams-scala" % "0.2.1",
  "io.confluent" % "kafka-streams-avro-serde" % confluentVersion,
  "org.typelevel" %% "cats-core" % "1.0.1"
  //"ch.qos.logback" % "logback-classic" % "1.2.3"
)


libraryDependencies += "com.typesafe" % "config" % "1.3.3"

libraryDependencies += "com.google.code.gson" % "gson" % "2.8.5"

libraryDependencies += "junit" % "junit" % "4.10" % Test

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.5"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"

libraryDependencies += "org.apache.kafka" % "kafka-streams-test-utils" % "1.1.1"

val logback = "1.2.3"
libraryDependencies += "ch.qos.logback" % "logback-core" % logback
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.8.0-alpha2"

libraryDependencies += "org.mongodb.scala" % "mongo-scala-driver_2.11" % "2.4.1"

