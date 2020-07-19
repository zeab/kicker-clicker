
//Imports
import Settings._
import Dependencies._
import Docker._

//Sbt Log Level
logLevel := Level.Info

//Add all the command alias's
CommandAlias.allCommandAlias

lazy val kickerclicker = (project in file("."))
  .settings(rootSettings: _*)
  .settings(libraryDependencies ++= rootDependencies)
  .settings(rootDockerSettings)
