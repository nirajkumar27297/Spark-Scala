/*
The objective of the controller is to get input from the presentation layer and pass it to
business layer and vice versa.
Library used :- playframework
  Version :- 2.8.2
@author:Niraj Kumar
 */

package controllers

import javax.inject.{Inject, Singleton}
import models.Utility
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContent, BaseController, ControllerComponents, Request}
import services.ClosePricePredictionService.{
  convertToJson,
  convertToXML,
  predictPrice
}

/*
The objective of the class is to get the input from the presentation layer pass values to business layer and
return responses in XML or JSON.
 */
@Singleton
class ClosePricePredictionController @Inject() (
    val controllerComponents: ControllerComponents
) extends BaseController {

  /*
  The function calls requests and return the response as json.
   */

  def predictPriceJson() = {

    Action { implicit request =>
      val tupleOutputMessageAndValue = predictPrice(request)
      val jsonOutput = convertToJson(tupleOutputMessageAndValue)
      Ok(jsonOutput)
    }
  }

  /*
  The function calls requests and return the response as XML.
   */

  def predictPriceXml() =
    Action { implicit request =>
      val tupleOutputMessageAndValue = predictPrice(request)
      val xmlOutput = convertToXML(tupleOutputMessageAndValue)
      Ok(xmlOutput).as("text/xml")
    }

  /*
  This function redirects to index page i.e. homePage
   */

  def homePage() = {
    Action { implicit request =>
      Ok(views.html.index())
    }
  }
}
