package modules.auth

import java.time.ZonedDateTime
import java.util.UUID

import javax.inject.Inject
import modules.auth.Forms._
import modules.user.{Role, User, UserRepository}
import play.api.data.{Form, FormError}
import play.api.data.Forms._
import modules.utility.form.FormUtilities._
import modules.utility.form.FormValidators

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

  val signUpForm = new Form(
    mapping(
      "username"         -> nonEmptyText,
      "email"            -> email,
      "password"         -> of(passwordFormatterWithValidator),
      "password-confirm" -> nonEmptyText
    )((username, email, password, _) => SignUpFormModel(username, email, password))(sufm =>
      Some(sufm.username, sufm.email, sufm.password, "")
    ),
    Map.empty,
    Seq.empty,
    None
  ) with WithAsyncValidator[SignUpFormModel] {

    override def asyncValidator(
        value: SignUpFormModel
    )(implicit ec: ExecutionContext): Map[String, Future[Option[String]]] = {
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
  }

//  .withAsyncValidator { (_, model) =>
//    val validations: Map[String, Future[Option[String]]] = Map(
//      "username" -> {
//        userRepository
//          .findByUsername(model.username)
//          .map {
//            case Some(_) =>
//              Some("validation.error.username.unique")
//            case _ =>
//              None
//          }
//      },
//      "email" -> {
//        userRepository
//          .findByEmail(model.email)
//          .map {
//            case Some(_) =>
//              Some("validation.error.email.unique")
//            case _ =>
//              None
//          }
//      }
//    )
//
//    val transformedValidations: Future[Map[String, String]] =
//      validations
//
//    transformedValidations
//  }
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
