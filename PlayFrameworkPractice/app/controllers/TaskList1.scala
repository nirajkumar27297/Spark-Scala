package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents, Request}
import views.html.helper
import models.TaskListInMemoryModel

@Singleton
class TaskList1 @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  def login = Action { implicit request =>
    Ok(views.html.login1())
  }

  def validateLoginGet(username:String,password:String) = Action {
    Ok(s"${username} logged in with ${password}")
  }

  def validateLoginPost() = Action { request =>
    val postVals = request.body.asFormUrlEncoded
    postVals.map{ args =>
      val username = args("username").head
      val password = args("password").head
      if(TaskListInMemoryModel.validateUser(username,password)) {
        Redirect(routes.TaskList1.taskList()).withSession("username" -> username)
      }
      else {
        Redirect(routes.TaskList1.login())
      }
    }.getOrElse(Redirect(routes.TaskList1.login()))
  }

  def taskList() = Action { request =>
    val usernameOption = request.session.get("username")
    usernameOption.map { username =>
      val tasks = TaskListInMemoryModel.getTasks(username)
      Ok(views.html.task1(tasks))
    }.getOrElse(Redirect(routes.TaskList1.login()))
  }

  def createUser() = Action { request =>
    val postVals = request.body.asFormUrlEncoded
    postVals.map { args =>
      val username = args("username").head
      val password = args("password").head
      if (TaskListInMemoryModel.createUser(username, password)) {
        Redirect(routes.TaskList1.taskList()).withSession("username" -> username)
      }
      else {
        Redirect(routes.TaskList1.login())
      }
    }.getOrElse(Redirect(routes.TaskList1.login()))
  }

  def logout = Action {
    Redirect(routes.TaskList1.login()).withNewSession
  }



}
