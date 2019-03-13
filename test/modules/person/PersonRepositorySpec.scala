package modules.person

import com.dimafeng.testcontainers.PostgreSQLContainer
import org.scalatest.TestData
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Application
import utility.application.TestApplications._
import utility.database.PlayPostgreSQLTest

import scala.concurrent.ExecutionContext

class PersonRepositorySpec extends PlaySpec
  with GuiceOneAppPerTest
  with PlayPostgreSQLTest
  with ScalaFutures
  with IntegrationPatience {

  override val container: PostgreSQLContainer = PostgreSQLContainer("postgres:alpine")

  override def newAppForTest(testData: TestData): Application = {
    basicDatabaseTestApplication(container, evolutionsEnabled = false)
  }

  "PersonRepository" must {
    "create new person John" in {
      implicit lazy val executionContext = app.injector.instanceOf[ExecutionContext]
      withEvolutions { () =>
        val repo = app.injector.instanceOf[PersonRepository]
        val person = repo.create("John", 24).futureValue

        person mustEqual Person(1, "John", 24)
      }
    }

    "create new person Alex and update age" in {
      implicit lazy val executionContext = app.injector.instanceOf[ExecutionContext]
      withEvolutions { () =>
        val repo = app.injector.instanceOf[PersonRepository]

        val newPerson = repo.create("Alex", 33).futureValue
        val updatedPerson = repo.save(newPerson.copy(age = 34)).futureValue

        updatedPerson mustEqual Person(1, "Alex", 34)
      }
    }
  }
}
