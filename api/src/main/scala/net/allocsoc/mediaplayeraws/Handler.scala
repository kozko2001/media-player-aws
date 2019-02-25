package net.allocsoc.mediaplayeraws

import com.amazonaws.services.lambda.runtime.events.{ APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent }
import com.amazonaws.services.lambda.runtime.{ Context, RequestHandler }
import scala.collection.JavaConverters._

class Handler extends RequestHandler[APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent] {

  def handleRequest(req: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent = {
    val logger = context.getLogger()

    logger.log("something!")

    return new APIGatewayProxyResponseEvent()
      .withBody("hello world!")
      .withStatusCode(200)
      .withHeaders(Map("Content-Type" -> "text/html; charset=utf-8").asJava)
  }
}
