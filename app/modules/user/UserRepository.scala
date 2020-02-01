package modules.user

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile

import scala.concurrent.Future

trait UserRepository {

  def findUserByLoginInfo(loginInfo: LoginInfo): Future[Option[User]]

  def findByEmail(email: String): Future[Option[User]]

  def add(user: User): Future[User]

  def add(email: String, profile: CommonSocialProfile): Future[User]
}
