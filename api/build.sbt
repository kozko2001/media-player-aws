import Dependencies._

ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / version          := "0.1"
ThisBuild / organization     := "net.allocsoc.media-player-aws"
ThisBuild / organizationName := "allocsoc"

lazy val root = (project in file("."))
  .settings(
    name := "MediaPlayerAwsAPI",
    libraryDependencies ++= Seq(
      scalaTest % Test,
      scalaMockito % Test,
      amazonlambda,
      amazons3,
      amazonevents,
      playjson,
      macwire
    )
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
