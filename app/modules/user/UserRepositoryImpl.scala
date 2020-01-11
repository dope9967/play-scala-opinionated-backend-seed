package modules.user
import com.mohiva.play.silhouette.api.LoginInfo
import javax.inject.Inject
import modules.auth.PasswordDataDAOComponent
import modules.utility.database.ExtendedPostgresProfile
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}

import scala.concurrent.{ExecutionContext, Future}

class UserRepositoryImpl @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider
)(
    implicit ec: ExecutionContext
) extends UserRepository
    with HasDatabaseConfigProvider[ExtendedPostgresProfile]
    with UserDAOComponent
    with PasswordDataDAOComponent {
  import profile.api._

  override def findUserByLoginInfo(
      loginInfo: LoginInfo
  ): Future[Option[User]] = {
    val query = userTable
      .join(passwordDataTable)
      .on {
        case (ut, pdt) =>
          ut.id === pdt.userId
      }
      .filter {
        case (_, pdt) =>
          pdt.providerKey === loginInfo.providerKey && pdt.providerKey === loginInfo.providerID
      }
      .map {
        case (ut, _) =>
          ut
      }
    db.run(query.result.headOption)
  }

  override def save(user: User): Future[User] = {
    val query = (userTable returning userTable) += user
    db.run(query).map(user => user)
  }
}
