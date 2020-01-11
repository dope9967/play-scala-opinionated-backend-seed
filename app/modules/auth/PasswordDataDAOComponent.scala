package modules.auth

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import modules.utility.database.{CustomDatabaseTypes, ExtendedPostgresProfile}
import play.api.db.slick.HasDatabaseConfig

trait PasswordDataDAOComponent extends CustomDatabaseTypes {
  self: HasDatabaseConfig[ExtendedPostgresProfile] =>
  import profile.api._

  val passwordDataTable = TableQuery[PasswordDataTable]

  class PasswordDataTable(tag: Tag) extends Table[PasswordData](tag, "password_data") {

    def providerID  = column[String]("provider_id")
    def providerKey = column[String]("provider_key")
    def hasher      = column[String]("hasher")
    def password    = column[String]("hasher")
    def salt        = column[Option[String]]("salt")
    def userId      = column[UUID]("user_id")

    def pk = primaryKey("password_data_pk", (providerID, providerKey))

    def loginInfoProjection =
      (providerID, providerKey) <> ((LoginInfo.apply _).tupled, LoginInfo.unapply)

    def passwordInfoProjection =
      (hasher, password, salt) <> ((PasswordInfo.apply _).tupled, PasswordInfo.unapply)

    def * =
      (
        loginInfoProjection,
        passwordInfoProjection
      ) <> ((PasswordData.apply _).tupled, PasswordData.unapply)
  }
}
