package net.allocsoc.mediaplayeraws

import com.amazonaws.AmazonServiceException
import com.amazonaws.services.lambda.runtime.events.{ APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent }
import com.amazonaws.services.lambda.runtime.{ Context, RequestHandler }
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.{ AmazonS3, AmazonS3Client, AmazonS3ClientBuilder }
import java.time.{ Duration, Instant }
import java.util.Date
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

class LambdaHandler(infoHandler: InfoHandler, unknownHandler: UnknownHandler, downloadHandler: (String) => DownloadHandler) {
  def errorResponse(message:String) = LambdaHandler.response(s"""{"error": "${message}"}""", 500)

  def handleRequest(req: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent = {

    val reqType = parseRequest(req)
    val handler = findHandler(reqType)

    try {
      handler.handler()
    } catch {
      case e: AmazonServiceException => return errorResponse(e.getMessage)
    }
  }

  private def findHandler(req: RequestType): MediaPlayerRequestHandler = {
    req match {
      case RequestInfo() => infoHandler
      case RequestDownload(file) => downloadHandler(file)
      case _ => unknownHandler
    }
  }

  private def parseRequest(req: APIGatewayProxyRequestEvent): RequestType = {
    val isDownload = """/download/(.*)""".r

    req.getPath match {
      case "/info.json" => RequestInfo()
      case isDownload(file) => RequestDownload(file)
      case _ => RequestUnknown()
    }
  }
}

object LambdaHandler {
  def response(body: String, status: Int = 200, headers: Option[Map[String, String]] = None) = {
    val defaultHeaders = Map("Content-Type" -> "application/json", "Access-Control-Allow-Origin" -> "*")

    new APIGatewayProxyResponseEvent()
      .withBody(body)
      .withStatusCode(status)
      .withHeaders( headers.getOrElse(defaultHeaders) .asJava)
  }
}


trait MediaPlayerRequestHandler {
  def handler(): APIGatewayProxyResponseEvent
}

class InfoHandler(s3Client: AmazonS3) extends MediaPlayerRequestHandler {
  implicit val infoResponseFormat = Json.format[InfoResponse]

  def handler() = {
    val files = listOfFilesInBucket()
    LambdaHandler.response(Json.toJson(InfoResponse(files)) toString)
  }

  private def listOfFilesInBucket() = {
    val bucketName = "media-player-aws-kzk"
    val prefix = "music/"

    val objects = s3Client.listObjects(bucketName, prefix)

    objects.getObjectSummaries.asScala .map(_.getKey) .toList
  }
}

class DownloadHandler(file: String, s3Client: AmazonS3) extends MediaPlayerRequestHandler {
  val bucketName = "media-player-aws-kzk"
  val expirationDate = Date.from (Instant.now().plus(Duration.ofHours(1)))

  def handler() = {
    val req = new GeneratePresignedUrlRequest(bucketName, file)
      .withExpiration(expirationDate)

    val url = s3Client.generatePresignedUrl(req).toString()

    LambdaHandler.response("", 301, Some(Map("Location" -> url)))
  }
}



class UnknownHandler() extends MediaPlayerRequestHandler {
  def handler() = LambdaHandler.response("""{"error": "Unknown command"}""", 404)
}
