package modules.utility

import org.scalatest.{Matchers, TestData, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Application
import play.api.db.DBApi
import utility.application.TestApplications.basicDatabaseTestApplication
import utility.database.PlayPostgreSQLTest

class EvolutionsSpec
    extends WordSpec
    with GuiceOneAppPerTest
    with PlayPostgreSQLTest
    with Matchers {

  override def newAppForTest(testData: TestData): Application = {
    basicDatabaseTestApplication(container, evolutionsEnabled = false)
  }

  "Evolutions" when {
    "bound" should {
      "bind database API as singleton" in {
        val api1 = app.injector.instanceOf[DBApi]
        val api2 = app.injector.instanceOf[DBApi]

        api1 shouldEqual api2
      }
    }

    "ran" should {
      "apply and unapply properly" in {
        applyEvolutions()

        unapplyEvolutions()

        applyEvolutions()

        unapplyEvolutions()
      }
    }
  }
}
