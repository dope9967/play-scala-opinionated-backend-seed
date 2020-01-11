package modules.auth

import com.mohiva.play.silhouette.api.{LoginInfo, Silhouette}
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{Credentials, PasswordHasherRegistry}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import javax.inject.Inject
import modules.user.UserRepository
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{
  AbstractController,
  Action,
  AnyContent,
  ControllerComponents,
  MessagesAbstractController,
  MessagesControllerComponents,
  Request
}
import views.html

import scala.concurrent.{ExecutionContext, Future}

class AuthController @Inject() (
    cc: ControllerComponents,
    silhouette: Silhouette[DefaultEnv],
    credentialsProvider: CredentialsProvider,
    authInfoRepository: AuthInfoRepository,
    passwordHasherRegistry: PasswordHasherRegistry,
    userRepository: UserRepository,
    forms: Forms
)(implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with I18nSupport {

  def index(): Action[AnyContent] = silhouette.UserAwareAction { implicit request =>
    implicit val authorizedUserOption: Option[AuthorizedUser] = request.identity
    Ok(views.html.index())
  }

  def unauthorized(): Action[AnyContent] = silhouette.UserAwareAction { implicit request =>
    implicit val authorizedUserOption: Option[AuthorizedUser] = request.identity
    Ok(views.html.unauthorized())
  }

  def loginView: Action[AnyContent] = silhouette.UnsecuredAction {
    implicit request: Request[AnyContent] =>
      Ok(views.html.login(forms.loginForm))
  }

  def login: Action[AnyContent] = silhouette.UnsecuredAction.async {
    implicit request: Request[AnyContent] =>
      forms.loginForm.bindFromRequest.fold(
        form => Future.successful(BadRequest(views.html.login(form))),
        data => {
          val credentials = Credentials(data.username, data.password)
          val result      = Redirect(routes.AuthController.index())
          credentialsProvider
            .authenticate(credentials)
            .flatMap { loginInfo =>
              silhouette.env.authenticatorService
                .create(loginInfo)
                .flatMap { authenticator =>
                  silhouette.env.authenticatorService.init(authenticator).flatMap { v =>
                    silhouette.env.authenticatorService.embed(v, result)
                  }
                }
            }
            .recover {
              case _: ProviderException =>
                Redirect(routes.AuthController.loginView())
                  .flashing("error" -> Messages("error.invalid.credentials"))
            }
        }
      )
  }

  def logout: Action[AnyContent] = silhouette.SecuredAction.async {
    implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
      val result = Redirect(routes.AuthController.index())
      silhouette.env.authenticatorService.discard(request.authenticator, result)
  }

  def signUpView: Action[AnyContent] = silhouette.UnsecuredAction {
    implicit request: Request[AnyContent] =>
      Ok(views.html.signup(forms.signUpForm))
  }

  def signUp(): Action[AnyContent] = silhouette.UnsecuredAction.async {
    implicit request: Request[AnyContent] =>
      forms.signUpForm
        .bindFromRequest()
        .fold(
          formWithErrors => {
            Future.successful(BadRequest(views.html.signup(formWithErrors)))
          },
          formModel => {
            //TODO needs more fault tolerance
            val user = formModel.produceUser()
            for {
              _ <- userRepository.save(user)
              _ <- {
                val loginInfo = LoginInfo(CredentialsProvider.ID, user.email)
                val authInfo  = passwordHasherRegistry.current.hash(formModel.password)
                authInfoRepository.add(loginInfo, authInfo)
              }
            } yield {
              Redirect(routes.AuthController.loginView())
                .flashing("success" -> Messages("signup.success"))
            }
          }
        )
  }
}
