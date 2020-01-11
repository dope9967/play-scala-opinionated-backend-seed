package modules.user

import java.time.{ZoneId, ZonedDateTime}
import java.util.UUID

import modules.user
import modules.user.Role.Role
import play.api.i18n.Lang
import play.api.libs.json.{Format, Json, OFormat}

case class User(
    id: UUID,
    username: String,
    email: String,
    role: Role,
    firstName: Option[String],
    lastName: Option[String],
    language: Option[Lang] = None,
    timeZone: Option[ZoneId] = None,
    enabled: Boolean = true,
    createDateTime: ZonedDateTime,
    updateDateTime: Option[ZonedDateTime] = None
)

object User {
  implicit val format: OFormat[User] = Json.format
}

object Role extends Enumeration {
  type Role = Value
  val Administrator, Common = Value

  implicit val format: Format[user.Role.Value] = Json.formatEnum(this)
}
