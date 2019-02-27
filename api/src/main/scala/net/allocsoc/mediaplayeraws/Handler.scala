package net.allocsoc.mediaplayeraws

import com.amazonaws.services.lambda.runtime.events.{ APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent }
import com.amazonaws.services.lambda.runtime.{ Context, RequestHandler }
import com.amazonaws.services.s3.{ AmazonS3, AmazonS3Client, AmazonS3ClientBuilder }
import scala.collection.JavaConverters._
import play.api.libs.json._
import collection.JavaConverters._

class Handler extends RequestHandler[APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent] {
  implicit val infoResponseFormat = Json.format[InfoResponse]
  var s3Client = AmazonS3ClientBuilder.defaultClient()

  def handleRequest(req: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent = {
    val logger = context.getLogger()

    val infoResponse = InfoResponse(handlerInfo.listOfFilesInBucket())

    return new APIGatewayProxyResponseEvent()
      .withBody(Json.toJson(infoResponse).toString())
      .withStatusCode(200)
      .withHeaders(Map("Content-Type" -> "text/html; charset=utf-8").asJava)
  }

  def handlerInfo() = new HandlerInfoJson(s3Client)

}

class HandlerInfoJson(s3Client: AmazonS3) {

  def listOfFilesInBucket() = {
    val bucketName = "media-player-aws-kzk"
    val prefix = "music/"

    val objects = s3Client.listObjects(bucketName, prefix)

    objects.getObjectSummaries.asScala .map(_.getKey) .toList
  }
}
