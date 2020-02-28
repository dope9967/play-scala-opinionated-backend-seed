package modules.auth

import modules.user.UserRepository
import modules.utility.twirl.CustomHelpers
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{Ignore, OptionValues, TestData}
import org.scalatestplus.play.guice.GuiceOneServerPerTest
import org.scalatestplus.play.{HtmlUnitFactory, OneBrowserPerTest, PlaySpec}
import play.api.Application
import utility.application.TestApplications.basicDatabaseTestApplication
import utility.database.PlayPostgreSQLTest

//TODO enable once Silhouette test bindings are added
@Ignore
class SignUpSpec
    extends PlaySpec
    with GuiceOneServerPerTest
    with OneBrowserPerTest
    with HtmlUnitFactory
    with PlayPostgreSQLTest
    with ScalaFutures
    with IntegrationPatience
    with OptionValues {

  override def newAppForTest(testData: TestData): Application = {
    basicDatabaseTestApplication(container)
  }

  "Sign up" must {
    "create user" in {
      {
        val url =
          s"http://localhost:$port${modules.auth.routes.AuthController.signUp().url}"
        go to url
      }

      eventually {
        pageTitle mustBe CustomHelpers.AppName
      }

      val username = "test"
      val email    = "test@email.com"
      val password = "qweqwe"

      click on name("username")
      textField("username").value = username

      click on name("email")
      textField("email").value = "test@email.com"

      click on name("password")
      textField("password").value = password

      click on name("password")
      textField("password").value = password

      click on id("submit")

      val repo = app.injector.instanceOf[UserRepository]

      eventually {
        val user = repo.findByUsername(username).futureValue.value
        user.username mustEqual username
        user.email mustEqual email
      }
    }
  }
}
