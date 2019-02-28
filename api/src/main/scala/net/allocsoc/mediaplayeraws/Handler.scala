package net.allocsoc.mediaplayeraws

import com.amazonaws.services.lambda.runtime.events.{ APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent }
import com.amazonaws.services.lambda.runtime.{ Context, RequestHandler }
import com.amazonaws.services.s3.{ AmazonS3, AmazonS3Client, AmazonS3ClientBuilder }
import scala.collection.JavaConverters._
import play.api.libs.json._
import collection.JavaConverters._

class Handler extends RequestHandler[APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent] with AppModule {

  def handleRequest(req: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent = {
    val handler = lambdaHandler

    handler.handleRequest(req, context)
  }
}

class LambdaHandler(infoHandler: InfoHandler) {
  def handleRequest(req: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent = {

    val response = infoHandler.handler()

    return new APIGatewayProxyResponseEvent()
      .withBody(response)
      .withStatusCode(200)
      .withHeaders(Map("Content-Type" -> "application/json").asJava)

  }
}

class InfoHandler(s3Client: AmazonS3) {
  implicit val infoResponseFormat = Json.format[InfoResponse]

  def handler(): String = {
    val files = listOfFilesInBucket()
    Json.toJson(InfoResponse(files)) toString
  }

  private def listOfFilesInBucket() = {
    val bucketName = "media-player-aws-kzk"
    val prefix = "music/"

    val objects = s3Client.listObjects(bucketName, prefix)

    objects.getObjectSummaries.asScala .map(_.getKey) .toList
  }
}
