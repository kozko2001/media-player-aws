package net.allocsoc.mediaplayeraws

import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.softwaremill.macwire._

trait AppModule {
  lazy val s3Client = AmazonS3ClientBuilder.defaultClient
  lazy val infoHandler = wire[InfoHandler]
  lazy val lambdaHandler = wire[LambdaHandler]
}
