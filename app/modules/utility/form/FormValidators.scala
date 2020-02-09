package modules.utility.form

import play.api.data.FormError
import play.api.data.format.Formatter

import scala.util.Try

trait FormValidators {

  lazy val passwordFormatterWithValidator = new Formatter[String] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] = {
      val password        = Try(data("password"))
      val passwordConfirm = Try(data("password-confirm"))

      if (password.getOrElse("").trim.isEmpty) {
        Left(List(FormError("password", "validation.error.password.required")))
      } else if (password != passwordConfirm) {
        Left(List(FormError("password", "validation.error.password.match.confirm")))
      } else {
        Right(password.get)
      }
    }

    override def unbind(key: String, value: String): Map[String, String] = {
      Map(key -> value)
    }
  }
}
