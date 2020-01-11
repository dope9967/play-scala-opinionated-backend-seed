package modules.auth

import com.mohiva.play.silhouette.api.Identity
import modules.user.User
import play.api.libs.json.{Json, OFormat}

case class AuthorizedUser(
    user: User
) extends Identity

object AuthorizedUser {
  implicit val format: OFormat[AuthorizedUser] = Json.format
}
