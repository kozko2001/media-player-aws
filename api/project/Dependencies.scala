import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5"
  lazy val amazonlambda = "com.amazonaws" % "aws-lambda-java-core" % "1.2.0"
  lazy val amazons3     = "com.amazonaws" % "aws-java-sdk-s3" % "1.11.505"
  lazy val amazonevents = "com.amazonaws" % "aws-lambda-java-events" % "2.2.5"
  lazy val scalaMockito = "org.mockito" % "mockito-scala_2.12" % "1.1.4"
  lazy val playjson     = "com.typesafe.play" % "play-json_2.12" % "2.6.10"
  lazy val macwire      ="com.softwaremill.macwire" %% "macros" % "2.3.1" % "provided"
}
