package controllers

import org.scalatestplus.play.PlaySpec
import play.api.http.Status.OK
import play.api.test.Helpers.{POST, contentAsString, contentType, defaultAwaitTimeout, status, stubControllerComponents}
import play.api.test.{FakeRequest}

class ClosePricePredictionControllerSpec extends PlaySpec {
  val jsonOutput = """{"messsage":"The predicted result is","value":"51.3","status":"200"}"""
  val xmlOutput = "<xml><data><message>The predicted result is</message><value>51.3</value></data><status>200</status></xml>"

  "ClosePricePredictionControllerSpec GET JSON" should {

    "Return JSON Output with Value as 51.43 message as The predicted result is and status 200" in {
      val controller = new ClosePricePredictionController(stubControllerComponents())
      val predictPriceJson = controller.predictPriceJson().apply(FakeRequest (POST, "/predict/Json").withFormUrlEncodedBody("OpenPrice" -> "49.8","HighPrice" -> "51.8","LowPrice" -> "49.8","Volume" -> "44871300"))

      status(predictPriceJson) mustBe OK
      contentType(predictPriceJson) mustBe Some("application/json")
      contentAsString(predictPriceJson) must include(jsonOutput)
    }
  }

  "ClosePricePredictionControllerSpec GET XML" should {

    "Return XML Output with Value as 51.43 message as The predicted result is and status 200" in {
      val controller = new ClosePricePredictionController(stubControllerComponents())
      val predictPriceXML = controller.predictPriceXml().apply(FakeRequest (POST, "/predict/Json").withFormUrlEncodedBody("OpenPrice" -> "49.8","HighPrice" -> "51.8","LowPrice" -> "49.8","Volume" -> "44871300"))

      status(predictPriceXML) mustBe OK
      contentType(predictPriceXML) mustBe Some("text/xml")
      contentAsString(predictPriceXML) must include(xmlOutput)
    }
  }
}
