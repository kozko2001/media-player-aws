package net.allocsoc.mediaplayeraws

import com.amazonaws.AmazonServiceException
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

  class AppModuleTest extends AppModule {
    override lazy val s3Client = mock[AmazonS3Client]
  }

  "Given a valid info.json request" when {
    val req = mock[APIGatewayProxyRequestEvent]
    val context = mock[Context]

    when(req.getPath) thenReturn "info.json"

    "the s3 bucket has 2 files" when {
      val objectListing = mock[ObjectListing]
      val obj1 = HandlerSpec.objectCreate("CelineDion-Titanic.mp3")
      val obj2 = HandlerSpec.objectCreate("LaMacarena")
      val module = new AppModuleTest()
      val s3Client = module.s3Client

      when(s3Client.listObjects(anyString(), anyString())) thenReturn objectListing
      when(objectListing.getObjectSummaries) thenReturn (obj1 :: obj2 :: Nil) .asJava

      ".handlerequest response has a couple of media files" in {
        val handler = module.lambdaHandler

        val response = handler.handleRequest(req, context)

        implicit val infoResponseFormat = Json.format[InfoResponse]
        val model = Json.fromJson[InfoResponse](Json.parse(response.getBody))

        response.getStatusCode shouldBe 200
        model.get.files should have size 2
        model.get.files{0} shouldBe "CelineDion-Titanic.mp3"
      }
    }

    "The s3 service is not available" when {
      val module = new AppModuleTest()
      val s3Client = module.s3Client

      when(s3Client.listObjects(anyString(), anyString())) thenThrow new AmazonServiceException("Ooops")

      ".handleRequest return code 500" in {
        val handler = module.lambdaHandler

        val response = handler.handleRequest(req, context)

        response.getStatusCode shouldBe 500
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
