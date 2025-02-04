ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.4"

lazy val root = (project in file("."))
  .settings(
    name := "untiled",
    libraryDependencies ++= Seq(
      "com.github.tototoshi" %% "scala-csv" % "2.0.0",
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.14.2",
      "com.typesafe.play" %% "play-json" % "2.10.0-RC5",
      "com.typesafe.play" %% "play-functional" % "2.10.0-RC5"
    )
  )