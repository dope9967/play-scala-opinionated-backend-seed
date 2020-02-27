package modules.utility

import org.scalatest.WordSpec
import play.api.Application
import utility.application.TestApplications.basicDatabaseTestApplication
import utility.database.PlayPostgreSQLTest

class EvolutionsSpec extends WordSpec with PlayPostgreSQLTest {

  override implicit def app: Application =
    basicDatabaseTestApplication(container, evolutionsEnabled = false)

  "Evolutions" when {
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
