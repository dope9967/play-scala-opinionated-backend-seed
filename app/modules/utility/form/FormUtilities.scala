package modules.utility.form

import play.api.data.{Form, FormError}
import play.api.mvc.Request

import scala.concurrent.{ExecutionContext, Future}

object FormUtilities {

  case class FormWithExtensions[T](
      form: Form[T],
      asyncValidator: (T, ExecutionContext) => Map[String, Future[Option[String]]]
  ) {

    def bindFromRequest()(implicit request: play.api.mvc.Request[_]): FormWithExtensions[T] = {
      copy(form.bindFromRequest())
    }

    def foldAsync[R](
        hasErrors: Form[T] => Future[R],
        success: T => Future[R]
    )(implicit request: Request[_], ec: ExecutionContext): Future[R] = {
      form
        .fold(
          formWithErrors => hasErrors(formWithErrors),
          formModel =>
            asyncValidator(formModel, ec)
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
                  form.copy(errors = asyncErrors.toSeq)
                case _ =>
                  form
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
