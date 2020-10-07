package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.{BaseController, ControllerComponents}

@Singleton
class Application @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  def product(prodType:String,prodNum:Int) = Action {
    Ok(s"The product type is ${prodType} and product number is ${prodNum}")
  }

}
