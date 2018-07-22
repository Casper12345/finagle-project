
scalaVersion := "2.12.6"

lazy val `finagle` = (project in file("."))
  .settings(
    name := "finagle",
    version := "0.1",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test
  ).enablePlugins(ScroogeSBT)
