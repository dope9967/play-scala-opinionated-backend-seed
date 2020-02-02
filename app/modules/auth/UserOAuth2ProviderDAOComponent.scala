package modules.auth

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import modules.utility.database.{CustomDatabaseTypes, ExtendedPostgresProfile}
import play.api.db.slick.HasDatabaseConfig

trait UserOAuth2ProviderDAOComponent extends CustomDatabaseTypes {
  self: HasDatabaseConfig[ExtendedPostgresProfile] =>
  import profile.api._

  val userOAuth2ProviderTable = TableQuery[UserOAuth2ProviderTable]

  class UserOAuth2ProviderTable(tag: Tag)
      extends Table[UserOAuth2Provider](tag, "user_oauth2_providers") {

    def userId      = column[UUID]("user_id")
    def providerID  = column[String]("provider_id")
    def providerKey = column[String]("provider_key")

    def pk = primaryKey("user_oauth2_providers_pk", (userId, providerID, providerKey))

    def loginInfoProjection =
      (providerID, providerKey) <> ((LoginInfo.apply _).tupled, LoginInfo.unapply)

    def * =
      (
        userId,
        loginInfoProjection
      ) <> ((UserOAuth2Provider.apply _).tupled, UserOAuth2Provider.unapply)
  }
}
