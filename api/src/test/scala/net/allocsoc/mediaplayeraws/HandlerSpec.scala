package net.allocsoc.mediaplayeraws

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import org.scalatest.{ Matchers, WordSpec }
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._
import play.api.libs.json._



class HandlerSpec extends WordSpec with MockitoSugar with Matchers  {
  "Given a valid info.json request" when {
    val req = mock[APIGatewayProxyRequestEvent]
    val context = mock[Context]

    when(req.getPath) thenReturn "info.json"

    ".handleRequest response has a couple of media files" in {
      val response = new Handler().handleRequest(req, context)

      implicit val infoResponseFormat = Json.format[InfoResponse]
      val model = Json.fromJson[InfoResponse](Json.parse(response.getBody))

      response.getStatusCode shouldBe 200
      model.get.files should have size 2
      model.get.files{0} shouldBe "CelineDion-Titanic.mp3"
    }
  }
}
