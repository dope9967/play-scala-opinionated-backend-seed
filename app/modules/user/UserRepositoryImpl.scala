package modules.user
import java.time.ZonedDateTime
import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.{CommonSocialProfile, SocialProviderRegistry}
import javax.inject.Inject
import modules.auth.{
  OAuth2DataDAOComponent,
  PasswordDataDAOComponent,
  UserOAuth2ProviderDAOComponent
}
import modules.utility.database.ExtendedPostgresProfile
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}

import scala.concurrent.{ExecutionContext, Future}

class UserRepositoryImpl @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider,
    socialProviderRegistry: SocialProviderRegistry
)(
    implicit ec: ExecutionContext
) extends UserRepository
    with HasDatabaseConfigProvider[ExtendedPostgresProfile]
    with UserDAOComponent
    with PasswordDataDAOComponent
    with OAuth2DataDAOComponent
    with UserOAuth2ProviderDAOComponent {
  import profile.api._

  override def findUserByLoginInfo(
      loginInfo: LoginInfo
  ): Future[Option[User]] = {
    loginInfo.providerID match {
      case "credentials" =>
        val query = userTable
          .join(passwordDataTable)
          .on {
            case (ut, pdt) =>
              ut.email === pdt.providerKey
          }
          .filter {
            case (_, pdt) =>
              pdt.providerKey === loginInfo.providerKey && pdt.providerID === loginInfo.providerID
          }
          .map {
            case (ut, _) =>
              ut
          }
        db.run(query.result.headOption)
      case social if socialProviderRegistry.providers.exists(_.id == social) =>
        val query = userTable
          .join(userOAuth2ProviderTable)
          .on {
            case (ut, uopd) =>
              ut.id === uopd.userId
          }
          .join(oauth2DataTable)
          .on {
            case ((_, uopd), odt) =>
              uopd.providerID === odt.providerID && uopd.providerKey === odt.providerKey
          }
          .filter {
            case (_, odt) =>
              odt.providerKey === loginInfo.providerKey && odt.providerID === loginInfo.providerID
          }
          .map {
            case ((ut, _), _) =>
              ut
          }
        db.run(query.result.headOption)
      case unhandled =>
        Future.failed(
          new IllegalArgumentException(
            s"Cannot find user by login info $loginInfo, unhandled provider $unhandled"
          )
        )
    }
  }

  override def findByEmail(email: String): Future[Option[User]] = {
    val query = userTable
      .filter(_.email === email)
    db.run(query.result.headOption)
  }

  override def findByUsername(username: String): Future[Option[User]] = {
    val query = userTable
      .filter(_.username === username)
    db.run(query.result.headOption)
  }

  override def add(user: User): Future[User] = {
    val query = (userTable returning userTable) += user
    db.run(query)
  }

  override def add(email: String, profile: CommonSocialProfile): Future[User] = {
    val user = User(
      id = UUID.randomUUID(),
      username = UUID.randomUUID().toString,
      email = email,
      role = Role.Common,
      firstName = profile.firstName,
      lastName = profile.lastName,
      createDateTime = ZonedDateTime.now()
    )
    val query = (userTable returning userTable) += user
    db.run(query)
  }
}
