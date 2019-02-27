package net.allocsoc.mediaplayeraws

import com.amazonaws.services.lambda.runtime.events.{ APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent }
import com.amazonaws.services.lambda.runtime.{ Context, RequestHandler }
import scala.collection.JavaConverters._
import play.api.libs.json._


class Handler extends RequestHandler[APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent] {
  implicit val infoResponseFormat = Json.format[InfoResponse]

  def handleRequest(req: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent = {
    val logger = context.getLogger()

    val infoResponse = InfoResponse("CelineDion-Titanic.mp3" :: "somethingElse.mp3" :: Nil)

    return new APIGatewayProxyResponseEvent()
      .withBody(Json.toJson(infoResponse).toString())
      .withStatusCode(200)
      .withHeaders(Map("Content-Type" -> "text/html; charset=utf-8").asJava)
  }
}
