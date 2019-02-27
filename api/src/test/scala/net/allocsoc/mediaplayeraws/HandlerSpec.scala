package net.allocsoc.mediaplayeraws

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.{ ObjectListing, S3ObjectSummary }
import org.scalatest.{ Matchers, WordSpec }
import org.scalatest.mockito.MockitoSugar
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import play.api.libs.json._
import scala.collection.JavaConverters._


class HandlerSpec extends WordSpec with MockitoSugar with Matchers  {
  "Given a valid info.json request" when {
    val req = mock[APIGatewayProxyRequestEvent]
    val context = mock[Context]

    when(req.getPath) thenReturn "info.json"

    "the s3 bucket has 2 files" when {
      val s3Client = mock[AmazonS3Client]
      val objectListing = mock[ObjectListing]
      val obj1 = HandlerSpec.objectCreate("CelineDion-Titanic.mp3")
      val obj2 = HandlerSpec.objectCreate("LaMacarena")

      when(s3Client.listObjects(anyString(), anyString())) thenReturn objectListing
      when(objectListing.getObjectSummaries) thenReturn (obj1 :: obj2 :: Nil) .asJava

      ".handleRequest response has a couple of media files" in {
        val handler = new Handler()
        handler.s3Client = s3Client

        val response = handler.handleRequest(req, context)

        implicit val infoResponseFormat = Json.format[InfoResponse]
        val model = Json.fromJson[InfoResponse](Json.parse(response.getBody))

        response.getStatusCode shouldBe 200
        model.get.files should have size 2
        model.get.files{0} shouldBe "CelineDion-Titanic.mp3"
      }
    }
  }
}

object HandlerSpec {
  def objectCreate(filename: String): S3ObjectSummary = {
    val s3Object = new S3ObjectSummary()
    s3Object.setKey(filename)
    return s3Object
  }
}
