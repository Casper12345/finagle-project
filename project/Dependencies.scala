import sbt._

object Dependencies {
  lazy val scalaTest = List(
    "org.scalamock" %% "scalamock" % "4.1.0",
    "org.scalatest" %% "scalatest" % "3.0.5" % Test
  )

}
