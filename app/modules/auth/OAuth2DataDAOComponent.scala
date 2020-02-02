package modules.auth

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import modules.utility.database.{CustomDatabaseTypes, ExtendedPostgresProfile}
import play.api.db.slick.HasDatabaseConfig

trait OAuth2DataDAOComponent extends CustomDatabaseTypes {
  self: HasDatabaseConfig[ExtendedPostgresProfile] =>
  import profile.api._

  val oauth2DataTable = TableQuery[OAuth2DataTable]

  class OAuth2DataTable(tag: Tag) extends Table[OAuth2Data](tag, "oauth2_data") {

    def providerID   = column[String]("provider_id")
    def providerKey  = column[String]("provider_key")
    def accessToken  = column[String]("access_token")
    def tokenType    = column[Option[String]]("token_type")
    def expiresIn    = column[Option[Int]]("expires_in")
    def refreshToken = column[Option[String]]("refresh_token")
    def params       = column[Option[Map[String, String]]]("params")

    def pk = primaryKey("oauth2_data_pk", (providerID, providerKey))

    def loginInfoProjection =
      (providerID, providerKey) <> ((LoginInfo.apply _).tupled, LoginInfo.unapply)

    def oauth2InfoProjection =
      (accessToken, tokenType, expiresIn, refreshToken, params) <> ((OAuth2Info.apply _).tupled, OAuth2Info.unapply)

    def * =
      (
        loginInfoProjection,
        oauth2InfoProjection
      ) <> ((OAuth2Data.apply _).tupled, OAuth2Data.unapply)
  }
}
