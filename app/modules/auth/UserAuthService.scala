package modules.auth

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import javax.inject.Inject
import modules.user.UserRepository

import scala.concurrent.{ExecutionContext, Future}

class UserAuthService @Inject() (
    userRepository: UserRepository
)(implicit ec: ExecutionContext)
    extends IdentityService[AuthorizedUser] {

  override def retrieve(loginInfo: LoginInfo): Future[Option[AuthorizedUser]] = {
    userRepository
      .findUserByLoginInfo(loginInfo)
      .map {
        case Some(user) =>
          Some(AuthorizedUser(user))
        case _ =>
          None
      }
  }
}
