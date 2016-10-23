name := """scalaio-miroservices-websocket"""

version := "0.0.1"

resolvers += Resolver.bintrayRepo("hseeberger", "maven")

val commonDependencies =  {
  val AkkaHttpVersion   = "2.4.11"
  val circeVersion      = "0.5.1"

  Seq(
    "com.typesafe.akka" %% "akka-http-experimental"   % AkkaHttpVersion,
    "de.heikoseeberger" %% "akka-http-circe"          % "1.10.1",
    "io.circe"          %% "circe-core"               % circeVersion,
    "io.circe"          %% "circe-generic"            % circeVersion,
    "io.circe"          %% "circe-parser"             % circeVersion,
    "me.lessis"         %% "tubesocks"                % "0.1.0",
    "org.jfarcand" % "wcs" % "1.5"
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
  .aggregate(api, auth, hotels, flights)

lazy val api = project.in(file("api"))
  .settings(projectSettings)
  .settings(libraryDependencies ++= commonDependencies)
  .dependsOn(common)

lazy val auth = project.in(file("microservices/auth"))
  .settings(projectSettings)
  .settings(libraryDependencies ++= commonDependencies)
  .dependsOn(common)

lazy val hotels = project.in(file("microservices/hotels"))
  .settings(projectSettings)
  .settings(libraryDependencies ++= commonDependencies)
  .dependsOn(common)

lazy val flights = project.in(file("microservices/flights"))
  .settings(projectSettings)
  .settings(libraryDependencies ++= commonDependencies)
  .dependsOn(common)

lazy val common = project.in(file("microservices/common"))
  .settings(projectSettings)
  .settings(libraryDependencies ++= commonDependencies)
