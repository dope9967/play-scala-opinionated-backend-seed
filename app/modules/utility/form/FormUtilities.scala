package modules.utility.form

import play.api.data.{Form, FormError}
import play.api.mvc.Request

import scala.concurrent.{ExecutionContext, Future}

object FormUtilities {

  trait WithAsyncValidator[T] extends Form[T] {

    def asyncValidator(value: T)(implicit ec: ExecutionContext): Map[String, Future[Option[String]]]

    def bindAndFoldAsync[R](
        hasErrors: Form[T] => Future[R],
        success: T => Future[R]
    )(implicit request: Request[_], ec: ExecutionContext): Future[R] = {
      val boundForm = bindFromRequest()
      boundForm
        .fold(
          formWithErrors => hasErrors(formWithErrors),
          formModel =>
            asyncValidator(formModel)
              .foldLeft(Future.successful(Map.empty[String, String])) {
                case (validationsFuture, (field, validation)) =>
                  validationsFuture
                    .flatMap { validations =>
                      validation
                        .map {
                          case Some(error) =>
                            validations + (field -> error)
                          case None =>
                            validations
                        }
                    }
              }
              .map { fieldErrorMap =>
                fieldErrorMap.map {
                  case (field, error) =>
                    FormError(
                      field,
                      error
                    )
                }
              }
              .map {
                case asyncErrors if asyncErrors.nonEmpty =>
                  boundForm.copy(errors = asyncErrors.toSeq)
                case _ =>
                  boundForm
              }
              .flatMap {
                case formWithErrors if formWithErrors.hasErrors =>
                  hasErrors(formWithErrors)
                case _ =>
                  success(formModel)
              }
        )
    }
  }
}
