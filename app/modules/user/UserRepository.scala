package modules.user

import com.mohiva.play.silhouette.api.LoginInfo

import scala.concurrent.Future

trait UserRepository {

  def findUserByLoginInfo(loginInfo: LoginInfo): Future[Option[User]]

  def save(user: User): Future[User]
}
