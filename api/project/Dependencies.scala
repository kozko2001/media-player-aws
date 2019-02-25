import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5"
  lazy val amazonlambda = "com.amazonaws" % "aws-lambda-java-core" % "1.2.0"
  lazy val amazons3     = "com.amazonaws" % "aws-java-sdk-s3" % "1.11.505"
  lazy val amazonevents = "com.amazonaws" % "aws-lambda-java-events" % "2.2.5"
}
