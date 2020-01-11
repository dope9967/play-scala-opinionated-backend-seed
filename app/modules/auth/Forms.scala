package modules.auth

import java.time.ZonedDateTime
import java.util.UUID

import javax.inject.Inject
import modules.auth.Forms._
import modules.user.{Role, User, UserRepository}
import modules.utility.FormValidators
import play.api.data.Form
import play.api.data.Forms._

class Forms @Inject() (
    userRepository: UserRepository
) extends FormValidators {

  val loginForm = Form(
    mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    )(LoginFormModel.apply)(LoginFormModel.unapply)
  )

  val signUpForm = Form(
    mapping(
      "username" -> nonEmptyText,
      //TODO implement proper async forms extension
//      .verifying(
//        "validation.error.username.unique",
//        value => Await.result(userRepository.checkUsernameUnique(value), 10.seconds)
//      )
      "email" -> email,
      //TODO implement proper async forms extension
//      .verifying(
//        "validation.error.email.unique",
//        value => Await.result(userRepository.checkUsernameUnique(value), 10.seconds)
//      )
      "password"         -> of(passwordFormatterWithValidator),
      "password-confirm" -> nonEmptyText
    )((username, email, password, _) => SignUpFormModel(username, email, password))(sufm =>
      Some(sufm.username, sufm.email, sufm.password, "")
    )
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
