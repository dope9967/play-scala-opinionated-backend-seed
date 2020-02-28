package modules.user

import java.time.ZonedDateTime
import java.util.UUID

import org.scalatest.TestData
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Application
import utility.application.TestApplications._
import utility.database.PlayPostgreSQLTest

class UserRepositorySpec
    extends PlaySpec
    with GuiceOneAppPerTest
    with PlayPostgreSQLTest
    with ScalaFutures
    with IntegrationPatience {

  override def newAppForTest(testData: TestData): Application = {
    basicDatabaseTestApplication(container)
  }

  "UserRepository" must {
    "create new test user" in {
      val repo = app.injector.instanceOf[UserRepository]

      val user = User(
        id = UUID.randomUUID(),
        username = "test",
        email = "test@email.com",
        role = Role.Common,
        firstName = None,
        lastName = None,
        createDateTime = ZonedDateTime.now()
      )

      repo.add(user).futureValue

      val createdUser = repo.findByUsername("test").futureValue.value

      createdUser.username mustEqual "test"
    }
  }
}
