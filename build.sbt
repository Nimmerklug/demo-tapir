val tapirVersion    = "1.10.9"
val AkkaVersion     = "2.8.5"
val AkkaHttpVersion = "10.5.3"

ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.3"
ThisBuild / organization := "com.softwaremill"

lazy val `tapir-http4s` = (project in file("tapir-http4s"))
  .settings(
    name         := "tapir-http4s",
    scalaVersion := "3.3.3",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir"   %% "tapir-http4s-server"      % tapirVersion,
      "org.http4s"                    %% "http4s-ember-server"      % "0.23.27",
      "com.softwaremill.sttp.tapir"   %% "tapir-prometheus-metrics" % tapirVersion,
      "com.softwaremill.sttp.tapir"   %% "tapir-swagger-ui-bundle"  % tapirVersion,
      "com.softwaremill.sttp.tapir"   %% "tapir-json-circe"         % tapirVersion,
      "ch.qos.logback"                 % "logback-classic"          % "1.5.6",
      "com.softwaremill.sttp.tapir"   %% "tapir-sttp-stub-server"   % tapirVersion % Test,
      "org.scalatest"                 %% "scalatest"                % "3.2.18"     % Test,
      "com.softwaremill.sttp.client3" %% "circe"                    % "3.9.7"      % Test
    )
  )

lazy val `tapir-akka` = (project in file("tapir-akka"))
  .settings(
    name         := "tapir-akka",
    scalaVersion := "2.13.14",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir"   %% "tapir-akka-http-server"   % tapirVersion,
      "com.typesafe.akka"             %% "akka-http"                % AkkaHttpVersion,
      "com.typesafe.akka"             %% "akka-actor-typed"         % AkkaVersion,
      "com.typesafe.akka"             %% "akka-stream"              % AkkaVersion,
      "com.softwaremill.sttp.tapir"   %% "tapir-prometheus-metrics" % tapirVersion,
      "com.softwaremill.sttp.tapir"   %% "tapir-swagger-ui-bundle"  % tapirVersion,
      "com.softwaremill.sttp.tapir"   %% "tapir-json-circe"         % tapirVersion,
      "ch.qos.logback"                 % "logback-classic"          % "1.5.6",
      "com.softwaremill.sttp.tapir"   %% "tapir-sttp-stub-server"   % tapirVersion % Test,
      "org.scalatest"                 %% "scalatest"                % "3.2.18"     % Test,
      "com.softwaremill.sttp.client3" %% "circe"                    % "3.9.7"      % Test
    )
  )

lazy val root = (project in file("."))
  .settings(
    name := "demo-tapir"
  )
  .aggregate(`tapir-http4s`, `tapir-akka`)
