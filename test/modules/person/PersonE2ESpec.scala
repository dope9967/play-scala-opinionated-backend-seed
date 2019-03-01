package modules.person

import com.dimafeng.testcontainers.PostgreSQLContainer
import org.scalatest.{OptionValues, TestData}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.play.{HtmlUnitFactory, OneBrowserPerTest, PlaySpec}
import org.scalatestplus.play.guice.GuiceOneServerPerTest
import play.api.Application
import utility.application.TestApplications.basicDatabaseTestApplication
import utility.database.PlayPostgreSQLTest

class PersonE2ESpec extends PlaySpec
  with GuiceOneServerPerTest
  with OneBrowserPerTest
  with HtmlUnitFactory
  with PlayPostgreSQLTest
  with ScalaFutures
  with IntegrationPatience
  with OptionValues {

  override val container: PostgreSQLContainer = PostgreSQLContainer("postgres:alpine")

  override def newAppForTest(testData: TestData): Application = {
    basicDatabaseTestApplication(container)
  }

  "Person form" must {
    "create person" in {
      {
        val url = s"http://localhost:$port${modules.person.routes.PersonController.createView().url}"
        go to url
      }

      eventually {
        pageTitle mustBe "Create person"
      }

      click on name("name")
      textField("name").value = "John"

      click on name("age")
      textField("age").value = "34"

      click on id("submit")

      val repo = app.injector.instanceOf[PersonRepository]
      val person = repo.find(1).futureValue.value
      person mustEqual Person(
        1,
        "John",
        34
      )
    }
  }
}
