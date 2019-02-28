package net.allocsoc.mediaplayeraws

import com.amazonaws.AmazonServiceException
import com.amazonaws.services.lambda.runtime.events.{ APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent }
import com.amazonaws.services.lambda.runtime.{ Context, RequestHandler }
import com.amazonaws.services.s3.{ AmazonS3, AmazonS3Client, AmazonS3ClientBuilder }
import scala.collection.JavaConverters._
import play.api.libs.json._
import collection.JavaConverters._


sealed trait RequestType

case class RequestInfo() extends RequestType
case class RequestDownload(file: String) extends RequestType
case class RequestUnknown() extends RequestType

class Handler extends RequestHandler[APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent] with AppModule {

  def handleRequest(req: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent = {
    val handler = lambdaHandler
    handler.handleRequest(req, context)
  }
}

class LambdaHandler(infoHandler: InfoHandler, unknownHandler: UnknownHandler) {
  def errorResponse(message:String) = new APIGatewayProxyResponseEvent()
    .withBody(s"""{"error": "${message}"}""") // TODO THIS SHOULD BE USING JSON!
    .withStatusCode(500)
    .withHeaders(Map("Content-Type" -> "application/json").asJava)

  def handleRequest(req: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent = {

    val reqType = parseRequest(req)
    val handler = findHandler(reqType)

    val response = try {
      handler.handler()
    } catch {
      case e: AmazonServiceException => return errorResponse(e.getMessage)
    }

    return new APIGatewayProxyResponseEvent()
      .withBody(response)
      .withStatusCode(200)
      .withHeaders(Map("Content-Type" -> "application/json").asJava)
  }

  private def findHandler(req: RequestType): MediaPlayerRequestHandler = {
    req match {
      case RequestInfo() => infoHandler
      case _ => unknownHandler
    }
  }

  private def parseRequest(req: APIGatewayProxyRequestEvent): RequestType = {
    req.getPath match {
      case "/info.json" => RequestInfo()
      case _ => RequestUnknown()
    }
  }
}

trait MediaPlayerRequestHandler {
  def handler(): String
}

class InfoHandler(s3Client: AmazonS3) extends MediaPlayerRequestHandler {
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

class UnknownHandler() extends MediaPlayerRequestHandler {
  def handler() = """{"error": "Unknown command"}"""
}
