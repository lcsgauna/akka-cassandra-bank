lazy val akkaHttpVersion = "10.2.8"
lazy val akkaVersion = "2.6.9"
lazy val circeVersion = "0.14.1"
lazy val javaDriverVersion = "4.13.0"
lazy val akkaPersistenceCassandraVersion = "1.0.5"
lazy val akkaHttpCirceVersion = "1.39.2"
lazy val logbackVersion = "1.2.10"
lazy val scalaTestVersion = "3.2.9"

lazy val root = (project in file("."))
  .settings(
    name := "akka-cassandra-bank",
    description := "Example of mini bank using Akka http, docker and cassandra",
    version := "0.1.0",
    scalaVersion := "2.13.8",
    sbtVersion := "1.6.2",
    libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-http"                  % akkaHttpVersion,
        "com.typesafe.akka" %% "akka-actor-typed"           % akkaVersion,
        "com.typesafe.akka" %% "akka-stream"                % akkaVersion,
        "com.typesafe.akka" %% "akka-persistence-typed"     % akkaVersion,
        "com.datastax.oss"  %  "java-driver-core"           % javaDriverVersion,
        "com.typesafe.akka" %% "akka-persistence-cassandra" % akkaPersistenceCassandraVersion,
        "io.circe"          %% "circe-core"                 % circeVersion,
        "io.circe"          %% "circe-generic"              % circeVersion,
        "io.circe"          %% "circe-parser"               % circeVersion,
        "de.heikoseeberger" %% "akka-http-circe"            % akkaHttpCirceVersion,
        "ch.qos.logback"    % "logback-classic"             % logbackVersion,
        "com.typesafe.akka" %% "akka-http-testkit"          % akkaHttpVersion % Test,
        "com.typesafe.akka" %% "akka-actor-testkit-typed"   % akkaVersion     % Test,
        "org.scalatest"     %% "scalatest"                  % scalaTestVersion % Test
    )
  )
