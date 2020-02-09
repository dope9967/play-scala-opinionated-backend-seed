package modules.auth

import java.time.ZonedDateTime
import java.util.UUID

import javax.inject.Inject
import modules.auth.Forms._
import modules.user.{Role, User, UserRepository}
import modules.utility.form.FormUtilities._
import modules.utility.form.FormValidators
import play.api.data.Form
import play.api.data.Forms._

import scala.concurrent.ExecutionContext

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

  val signUpForm = FormWithExtensions[SignUpFormModel](
    form = Form(
      mapping(
        "username"         -> nonEmptyText,
        "email"            -> email,
        "password"         -> of(passwordFormatterWithValidator),
        "password-confirm" -> nonEmptyText
      )((username, email, password, _) => SignUpFormModel(username, email, password))(sufm =>
        Some(sufm.username, sufm.email, sufm.password, "")
      )
    ),
    asyncValidator = (value, ec) => {
      implicit val implicitEc: ExecutionContext = ec
      Map(
        "username" -> {
          userRepository
            .findByUsername(value.username)
            .map {
              case Some(_) =>
                Some("validation.error.username.unique")
              case _ =>
                None
            }
        },
        "email" -> {
          userRepository
            .findByEmail(value.email)
            .map {
              case Some(_) =>
                Some("validation.error.email.unique")
              case _ =>
                None
            }
        }
      )
    }
  )
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
