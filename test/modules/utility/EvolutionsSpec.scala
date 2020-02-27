package modules.utility

import org.scalatest.WordSpec
import play.api.Application
import play.api.db.DBApi
import utility.application.TestApplications.basicDatabaseTestApplication
import utility.database.PlayPostgreSQLTest

class EvolutionsSpec extends WordSpec with PlayPostgreSQLTest {

  override implicit def app: Application =
    basicDatabaseTestApplication(container, evolutionsEnabled = false)

  "Evolutions" when {
    "ran" should {
      "apply and unapply properly" in {
        lazy val databaseApi = app.injector.instanceOf[DBApi]
        val database         = databaseApi.database("default")

        applyEvolutions(database)

        unapplyEvolutions(database)

        applyEvolutions(database)

        unapplyEvolutions(database)
      }
    }
  }
}
