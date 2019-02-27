package net.allocsoc.mediaplayeraws

import com.amazonaws.services.lambda.runtime.events.{ APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent }
import com.amazonaws.services.lambda.runtime.{ Context, RequestHandler }
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import scala.collection.JavaConverters._
import play.api.libs.json._
import collection.JavaConverters._

class Handler extends RequestHandler[APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent] {
  implicit val infoResponseFormat = Json.format[InfoRespon
    se]

  def handleRequest(req: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent = {
    val logger = context.getLogger()

    val infoResponse = InfoResponse(listOfFilesInBucket())

    return new APIGatewayProxyResponseEvent()
      .withBody(Json.toJson(infoResponse).toString())
      .withStatusCode(200)
      .withHeaders(Map("Content-Type" -> "text/html; charset=utf-8").asJava)
  }

  def listOfFilesInBucket() = {
    val bucketName = "media-player-aws-kzk"
    val prefix = "music/"

    val s3 = AmazonS3ClientBuilder.defaultClient();
    val objects = s3.listObjects(bucketName, prefix)

    objects.getObjectSummaries.asScala .map(_.getKey) .toList
  }
}
