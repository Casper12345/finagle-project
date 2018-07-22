
import Dependencies._

lazy val `finagle` = (project in file("."))
  .settings(
    name := "finagle",
    version := "0.1",
    scalaVersion := "2.12.6",
    libraryDependencies ++= scalaTest
  ).enablePlugins(ScroogeSBT)
