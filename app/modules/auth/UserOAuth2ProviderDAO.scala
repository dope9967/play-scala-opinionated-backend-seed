package modules.auth

import javax.inject.Inject
import modules.utility.database.ExtendedPostgresProfile
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}

import scala.concurrent.{ExecutionContext, Future}

class UserOAuth2ProviderDAO @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit val ec: ExecutionContext)
    extends HasDatabaseConfigProvider[ExtendedPostgresProfile]
    with UserOAuth2ProviderDAOComponent {
  import profile.api._

  def save(
      userOAuth2Provider: UserOAuth2Provider
  ): Future[UserOAuth2Provider] = {
    val query = userOAuth2ProviderTable.insertOrUpdate(userOAuth2Provider)
    db.run(query).map(_ => userOAuth2Provider)
  }
}
