name := "scalaio-miroservices-akka-remote"

version := "0.0.1"

resolvers += "patricknoir-bintray" at "https://dl.bintray.com/patricknoir/releases"

val commonDependencies =  {
  val AkkaV = "2.4.11"
  val circeV = "0.4.1" // limited by kafka-reactive-service

  Seq(
    "com.typesafe.akka" %% "akka-actor"               % AkkaV,
    "com.typesafe.akka" %% "akka-remote"              % AkkaV,
    "com.typesafe.akka" %% "akka-http-experimental"   % AkkaV,
    "de.heikoseeberger" %% "akka-http-circe"          % "1.9.0",  // limited by kafka-reactive-service
    "io.circe"          %% "circe-core"               % circeV,
    "io.circe"          %% "circe-generic"            % circeV,
    "io.circe"          %% "circe-parser"             % circeV,
    "org.patricknoir.kafka" %% "kafka-reactive-service" % "0.2.0"
  )
}


lazy val projectSettings = Seq(
  scalaVersion := "2.11.8",
  fork in Test := true
)

def baseProject(name: String): Project = {
  Project(name, file(name)).settings(projectSettings: _*)
}


lazy val root = project.in(file("."))
  .aggregate(api, auth)

lazy val api = project.in(file("api"))
  .settings(projectSettings)
  .settings(libraryDependencies ++= commonDependencies)

lazy val auth = project.in(file("microservices/auth"))
  .settings(projectSettings)
  .settings(libraryDependencies ++= commonDependencies)

lazy val flights = project.in(file("microservices/flights"))
  .settings(projectSettings)
  .settings(libraryDependencies ++= commonDependencies)

lazy val hotels = project.in(file("microservices/hotels"))
  .settings(projectSettings)
  .settings(libraryDependencies ++= commonDependencies)
