name := "scalaio-miroservices-akka-remote"

version := "0.0.1"

scalaVersion := "2.11.7"

libraryDependencies ++= {
  val AkkaV = "2.4.11"
  val circeV = "0.5.1"

  Seq(
    "com.typesafe.akka" %% "akka-actor"               % AkkaV,
    "com.typesafe.akka" %% "akka-remote"              % AkkaV,
    "com.typesafe.akka" %% "akka-http-experimental"   % AkkaV,
    "de.heikoseeberger" %% "akka-http-circe"          % "1.10.1",
    "io.circe"          %% "circe-core"               % circeV,
    "io.circe"          %% "circe-generic"            % circeV,
    "io.circe"          %% "circe-parser"             % circeV
  )
}
