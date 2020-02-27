package utility.database

import com.dimafeng.testcontainers.{ForEachTestContainer, PostgreSQLContainer}
import org.scalatest.Suite
import org.scalatestplus.play.AppProvider
import play.api.db.Database
import play.api.db.evolutions.Evolutions

trait PlayPostgreSQLTest extends Suite with AppProvider with ForEachTestContainer {

  override final val container: PostgreSQLContainer = PostgreSQLContainer("postgres:12.1-alpine")

  def applyEvolutions(database: Database): Unit = {
    Evolutions.applyEvolutions(database)
  }

  def unapplyEvolutions(database: Database): Unit = {
    Evolutions.cleanupEvolutions(database)
  }

  def withEvolutions(database: Database)(
      block: () => Unit
  ): Unit = {
    applyEvolutions(database)
    block()
    unapplyEvolutions(database)
  }

  //TODO consider requiring test database configuration so DBApi.database returns an already initialized instance
}
