package modules.auth

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import javax.inject.Inject
import modules.utility.database.ExtendedPostgresProfile
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class OAuth2InfoDAO @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit val classTag: ClassTag[OAuth2Info], ec: ExecutionContext)
    extends DelegableAuthInfoDAO[OAuth2Info]
    with HasDatabaseConfigProvider[ExtendedPostgresProfile]
    with OAuth2DataDAOComponent {
  import profile.api._

  override def find(loginInfo: LoginInfo): Future[Option[OAuth2Info]] = {
    val query = oauth2DataTable
      .filter { odt =>
        odt.providerKey === loginInfo.providerKey && odt.providerID === loginInfo.providerID
      }
    db.run(query.result.headOption)
      .map {
        case Some(data) =>
          Some(data.oauth2Info)
        case _ =>
          None
      }
  }

  override def add(
      loginInfo: LoginInfo,
      authInfo: OAuth2Info
  ): Future[OAuth2Info] = {
    val query = oauth2DataTable += OAuth2Data(loginInfo, authInfo)
    db.run(query).map(_ => authInfo)
  }

  override def update(
      loginInfo: LoginInfo,
      authInfo: OAuth2Info
  ): Future[OAuth2Info] = {
    val query = oauth2DataTable
      .filter { pdt =>
        pdt.providerKey === loginInfo.providerKey && pdt.providerID === loginInfo.providerID
      }
      .map(_.oauth2InfoProjection)
      .update(authInfo)
    db.run(query)
      .flatMap {
        case 0 =>
          Future.failed(
            new IllegalStateException(
              s"Could not update OAuth2 info, login info ${Json.toJson(loginInfo)} not found"
            )
          )
        case _ =>
          Future.successful(authInfo)
      }
  }

  override def save(
      loginInfo: LoginInfo,
      authInfo: OAuth2Info
  ): Future[OAuth2Info] = {
    val query = oauth2DataTable.insertOrUpdate(OAuth2Data(loginInfo, authInfo))
    db.run(query).map(_ => authInfo)
  }

  override def remove(loginInfo: LoginInfo): Future[Unit] = {
    val query = oauth2DataTable
      .filter(pdt =>
        pdt.providerKey === loginInfo.providerKey && pdt.providerID === loginInfo.providerID
      )
      .delete
    db.run(query).map(_ => ())
  }
}
