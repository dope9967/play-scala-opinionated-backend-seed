package modules.auth

import java.time.ZonedDateTime
import java.util.UUID

import javax.inject.Inject
import modules.auth.Forms._
import modules.user.{Role, User, UserRepository}
import modules.utility.FormValidators
import play.api.data.{Form, FormError}
import play.api.data.Forms._
import modules.utility.form.FormUtilities._

import scala.concurrent.{ExecutionContext, Future}

class Forms @Inject() (
    userRepository: UserRepository
)(implicit ec: ExecutionContext)
    extends FormValidators {

  val loginForm = Form(
    mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    )(LoginFormModel.apply)(LoginFormModel.unapply)
  )

  val signUpForm = Form(
    mapping(
      "username"         -> nonEmptyText,
      "email"            -> email,
      "password"         -> of(passwordFormatterWithValidator),
      "password-confirm" -> nonEmptyText
    )((username, email, password, _) => SignUpFormModel(username, email, password))(sufm =>
      Some(sufm.username, sufm.email, sufm.password, "")
    )
  ).withAsyncValidator { (form, model) =>
    Future
      .sequence(
        Seq(
          {
            userRepository
              .findByUsername(model.username)
              .map {
                case Some(_) =>
                  Some("username" -> "validation.error.username.unique")
                case _ =>
                  None
              }
          }, {
            userRepository
              .findByEmail(model.email)
              .map {
                case Some(_) =>
                  Some("email" -> "validation.error.email.unique")
                case _ =>
                  None
              }
          }
        )
      )
      .map(_.flatten)
      .map {
        case asyncErrors if asyncErrors.nonEmpty =>
          form.copy(
            errors = asyncErrors.map {
              case (key, error) =>
                FormError(
                  key,
                  error
                )
            }
          )
        case _ =>
          form
      }
  }
}

object Forms {

  case class LoginFormModel(
      username: String,
      password: String
  )

  case class SignUpFormModel(
      username: String,
      email: String,
      password: String
  ) {
    def produceUser(): User = {
      User(
        id = UUID.randomUUID(),
        username = username,
        email = email,
        role = Role.Common,
        firstName = None,
        lastName = None,
        createDateTime = ZonedDateTime.now()
      )
    }
  }
}
