name := "akka-http-oauth2-example"

version := "1.0"

scalaVersion := "2.12.2"

libraryDependencies ++=  dependencies.libs

lazy val dependencies = new {
  object Version {
    val akka         = "2.5.3"
    val akkaHttp     = "10.0.9"
    val akkaHttpJson = "1.16.0"
    val akkaLog4j    = "1.4.0"
    val akkaSse      = "3.0.0"
    val circe        = "0.8.0"
    val levelDb      = "0.9"
    val log4j        = "2.8.2"
    val scalaCheck   = "1.13.5"
    val scalaTest    = "3.0.3"
  }

  val libs = Seq(
    "com.typesafe.akka"        %% "akka-http"           % Version.akkaHttp,
    "de.heikoseeberger"        %% "akka-http-circe"     % Version.akkaHttpJson,
    "com.typesafe.akka"        %% "akka-http-testkit"   % Version.akkaHttp % Test,
    "io.circe"                 %% "circe-generic"       % Version.circe,
    "io.circe"                 %% "circe-parser"       % Version.circe,
    "io.circe"                 %% "circe-java8"       % Version.circe,
    "org.scalatest"            %% "scalatest"           % Version.scalaTest % Test)
}

        