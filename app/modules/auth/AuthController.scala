package modules.auth

import com.mohiva.play.silhouette.api.{LoginEvent, LoginInfo, LogoutEvent, SignUpEvent, Silhouette}
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{Credentials, PasswordHasherRegistry}
import com.mohiva.play.silhouette.impl.providers.{
  CommonSocialProfileBuilder,
  CredentialsProvider,
  SocialProvider,
  SocialProviderRegistry
}
import javax.inject.Inject
import modules.user.UserRepository
import play.api.Logger
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
import scala.util.Try

class AuthController @Inject() (
    cc: ControllerComponents,
    silhouette: Silhouette[DefaultEnv],
    credentialsProvider: CredentialsProvider,
    userAuthService: UserAuthService,
    authInfoRepository: AuthInfoRepository,
    passwordHasherRegistry: PasswordHasherRegistry,
    socialProviderRegistry: SocialProviderRegistry,
    userRepository: UserRepository,
    userOAuth2ProviderDAO: UserOAuth2ProviderDAO,
    forms: Forms
)(implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with I18nSupport {

  private val logger = Logger(this.getClass)

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
      Ok(views.html.login(forms.loginForm, socialProviderRegistry))
  }

  def login: Action[AnyContent] = silhouette.UnsecuredAction.async {
    implicit request: Request[AnyContent] =>
      forms.loginForm.bindFromRequest.fold(
        form => Future.successful(BadRequest(views.html.login(form, socialProviderRegistry))),
        data => {
          val credentials = Credentials(data.username, data.password)
          val result      = Redirect(routes.AuthController.index())
          credentialsProvider
            .authenticate(credentials)
            .flatMap { loginInfo =>
              //TODO add event logging
              //silhouette.env.eventBus.publish(LoginEvent(user, request))

              silhouette.env.authenticatorService
                .create(loginInfo)
                .flatMap { authenticator =>
                  silhouette.env.authenticatorService.init(authenticator).flatMap { v =>
                    silhouette.env.authenticatorService.embed(v, result)
                  }
                }
            }
            .recover {
              case e: ProviderException =>
                logger.error("Unexpected provider error", e)
                Redirect(routes.AuthController.loginView())
                  .flashing("error" -> Messages("error.invalid.credentials"))
            }
        }
      )
  }

  def logout: Action[AnyContent] = silhouette.SecuredAction.async {
    implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
      val result = Redirect(routes.AuthController.index())
      silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
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
            //TODO needs
            //TODO needs more fault tolerance
            val user = formModel.produceUser()
            for {
              _ <- userRepository.add(user)
              _ <- {
                val loginInfo = LoginInfo(CredentialsProvider.ID, user.email)
                val authInfo  = passwordHasherRegistry.current.hash(formModel.password)
                authInfoRepository.add(loginInfo, authInfo)
              }
            } yield {
              silhouette.env.eventBus.publish(SignUpEvent(AuthorizedUser(user), request))
              Redirect(routes.AuthController.loginView())
                .flashing("success" -> Messages("signup.success"))
            }
          }
        )
  }

  def authenticate(provider: String): Action[AnyContent] = Action.async {
    implicit request: Request[AnyContent] =>
      (socialProviderRegistry.get[SocialProvider](provider) match {
        case Some(p: SocialProvider with CommonSocialProfileBuilder) =>
          p.authenticate().flatMap {
            case Left(result) => Future.successful(result)
            case Right(authInfo) =>
              for {
                profile <- p.retrieveProfile(authInfo)
                //TODO consider not using email as linking data - might cause issues if user wants to change email,
                // maybe use email only for initial link, first trying by ID retrieved from social provider
                email <- Future.fromTry(
                  Try(
                    profile.email
                      .getOrElse(
                        throw new IllegalArgumentException(
                          s"Cannot create user from profile $profile, email not provided"
                        )
                      )
                  )
                )
                user <- userRepository
                  .findByEmail(email)
                  .flatMap {
                    case Some(user) =>
                      Future.successful(user)
                    case _ =>
                      userRepository.add(email, profile)
                  }
                _             <- authInfoRepository.save(profile.loginInfo, authInfo)
                _             <- userOAuth2ProviderDAO.save(UserOAuth2Provider(user.id, profile.loginInfo))
                authenticator <- silhouette.env.authenticatorService.create(profile.loginInfo)
                value         <- silhouette.env.authenticatorService.init(authenticator)
                result <- silhouette.env.authenticatorService
                  .embed(value, Redirect(routes.AuthController.index()))
              } yield {
                logger.debug(s"Retrieved profile $profile")
                silhouette.env.eventBus.publish(LoginEvent(AuthorizedUser(user), request))
                result
              }
          }
        case _ =>
          Future.failed(
            new ProviderException(s"Cannot authenticate with unexpected social provider $provider")
          )
      }).recover {
        case e: ProviderException =>
          logger.error("Unexpected provider error", e)
          Redirect(routes.AuthController.loginView())
            .flashing("error" -> Messages("could.not.authenticate"))
      }
  }
}
