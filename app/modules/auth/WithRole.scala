package modules.auth

import com.mohiva.play.silhouette.api.{Authenticator, Authorization}
import modules.user.Role.Role
import play.api.mvc.Request

import scala.concurrent.Future

case class WithRole[A <: Authenticator](role: Role) extends Authorization[AuthorizedUser, A] {

  override def isAuthorized[B](
      identity: AuthorizedUser,
      authenticator: A
  )(implicit request: Request[B]): Future[Boolean] = {
    Future.successful(identity.user.role == role)
  }
}
