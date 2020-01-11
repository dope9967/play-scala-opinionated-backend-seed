package modules.auth

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import javax.inject.Inject
import modules.utility.database.ExtendedPostgresProfile
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class PasswordInfoDAO @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit val classTag: ClassTag[PasswordInfo], ec: ExecutionContext)
    extends DelegableAuthInfoDAO[PasswordInfo]
    with HasDatabaseConfigProvider[ExtendedPostgresProfile]
    with PasswordDataDAOComponent {
  import profile.api._

  override def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = {
    val query = passwordDataTable
      .filter { pdt =>
        pdt.providerKey === loginInfo.providerKey && pdt.providerKey === loginInfo.providerID
      }
    db.run(query.result.headOption)
      .map {
        case Some(data) =>
          Some(data.passwordInfo)
        case _ =>
          None
      }
  }

  override def add(
      loginInfo: LoginInfo,
      authInfo: PasswordInfo
  ): Future[PasswordInfo] = {
    val query = passwordDataTable += PasswordData(loginInfo, authInfo)
    db.run(query).map(_ => authInfo)
  }

  override def update(
      loginInfo: LoginInfo,
      authInfo: PasswordInfo
  ): Future[PasswordInfo] = {
    val query = passwordDataTable
      .filter { pdt =>
        pdt.providerKey === loginInfo.providerKey && pdt.providerKey === loginInfo.providerID
      }
      .map(_.passwordInfoProjection)
      .update(authInfo)
    db.run(query)
      .flatMap {
        case 0 =>
          Future.failed(
            new IllegalStateException(
              s"Could not update password info, login info ${Json.toJson(loginInfo)} not found"
            )
          )
        case _ =>
          Future.successful(authInfo)
      }
  }

  override def save(
      loginInfo: LoginInfo,
      authInfo: PasswordInfo
  ): Future[PasswordInfo] = {
    val query = passwordDataTable.insertOrUpdate(PasswordData(loginInfo, authInfo))
    db.run(query).map(_ => authInfo)
  }

  override def remove(loginInfo: LoginInfo): Future[Unit] = {
    val query = passwordDataTable
      .filter(pdt =>
        pdt.providerKey === loginInfo.providerKey && pdt.providerKey === loginInfo.providerID
      )
      .delete
    db.run(query).map(_ => ())
  }
}
