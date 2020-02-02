package modules.utility.form

import play.api.data.Form

import scala.concurrent.{ExecutionContext, Future}

object FormUtilities {

  implicit class FormExtensions[T](form: Form[T]) {
    def withAsyncValidator(
        asyncValidator: (Form[T], T) => Future[Form[T]]
    ): FormWithAsyncValidator[T] = {
      FormWithAsyncValidator(form, asyncValidator)
    }
  }

  case class FormWithAsyncValidator[T](
      form: Form[T],
      asyncValidator: (Form[T], T) => Future[Form[T]]
  ) {

    def bindFromRequest()(implicit request: play.api.mvc.Request[_]): FormWithAsyncValidator[T] = {
      FormWithAsyncValidator(form.bindFromRequest(), asyncValidator)
    }

    def foldAsync[R](
        hasErrors: Form[T] => Future[R],
        success: T => Future[R]
    )(implicit ec: ExecutionContext): Future[R] = {
      form.fold(
        formWithErrors => hasErrors(formWithErrors),
        formModel =>
          asyncValidator(form, formModel).flatMap {
            case formWithErrors if formWithErrors.hasErrors =>
              hasErrors(formWithErrors)
            case _ =>
              success(formModel)
          }
      )
    }
  }
}
