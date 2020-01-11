package modules.user

import java.time.{ZoneId, ZonedDateTime}
import java.util.UUID

import modules.user.Role.Role
import modules.utility.database.{CustomDatabaseTypes, ExtendedPostgresProfile}
import play.api.db.slick.HasDatabaseConfig
import play.api.i18n.Lang

trait UserDAOComponent extends CustomDatabaseTypes {
  self: HasDatabaseConfig[ExtendedPostgresProfile] =>
  import profile.api._

  implicit val roleColumnType = MappedColumnType.base[Role, String](
    { enumType: Role =>
      enumType.toString
    }, { value: String =>
      Role.withName(value)
    }
  )

  val userTable = TableQuery[UserTable]

  class UserTable(tag: Tag) extends Table[User](tag, "users") {

    def id             = column[UUID]("id", O.PrimaryKey)
    def username       = column[String]("username")
    def email          = column[String]("email") //TODO needs citext for case insensitive unique index
    def role           = column[Role]("role")
    def firstName      = column[Option[String]]("first_name")
    def lastName       = column[Option[String]]("last_name")
    def language       = column[Option[Lang]]("language")
    def timeZone       = column[Option[ZoneId]]("time_zone")
    def enabled        = column[Boolean]("enabled")
    def createDateTime = column[ZonedDateTime]("create_date_time")
    def updateDateTime = column[Option[ZonedDateTime]]("update_date_time")

    def idx = index("users_email_idx", email, unique = true)

    def * =
      (
        id,
        username,
        email,
        role,
        firstName,
        lastName,
        language,
        timeZone,
        enabled,
        createDateTime,
        updateDateTime
      ) <> ((User.apply _).tupled, User.unapply)
  }
}
