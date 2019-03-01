package modules.person

import javax.inject._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.libs.json.Json
import play.api.mvc._
import views.html

import scala.concurrent.{ExecutionContext, Future}

class PersonController @Inject()(
    repo: PersonRepository,
    cc: MessagesControllerComponents
)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val personForm: Form[CreatePersonForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "age" -> number.verifying(min(0), max(140))
    )(CreatePersonForm.apply)(CreatePersonForm.unapply)
  }

  def index: Action[AnyContent] = Action { implicit request =>
    Ok(html.index())
  }

  def list = Action.async { implicit request =>
    repo.list().map(persons => Ok(html.person.list(persons)))
  }

  def createView = Action { implicit request =>
    Ok(html.person.create(personForm))
  }

  def create: Action[AnyContent] = Action.async { implicit request =>
    personForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(html.person.create(errorForm)))
      },
      person => {
        repo.create(person.name, person.age).map { _ =>
          Redirect(routes.PersonController.list()).flashing("success" -> "user.created")
        }
      }
    )
  }
}
