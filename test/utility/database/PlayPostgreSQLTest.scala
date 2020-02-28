package utility.database

import com.dimafeng.testcontainers.{ForEachTestContainer, PostgreSQLContainer}
import org.scalatest.Suite
import org.scalatestplus.play.AppProvider
import play.api.db.DBApi
import play.api.db.evolutions.Evolutions

trait PlayPostgreSQLTest extends Suite with AppProvider with ForEachTestContainer {

  override final val container: PostgreSQLContainer = PostgreSQLContainer("postgres:12.1-alpine")

  def applyEvolutions(): Unit = {
    val database = app.injector.instanceOf[DBApi].database("default")
    Evolutions.applyEvolutions(database)
  }

  def unapplyEvolutions(): Unit = {
    val database = app.injector.instanceOf[DBApi].database("default")
    Evolutions.cleanupEvolutions(database)
  }

  def withEvolutions(block: () => Unit): Unit = {
    applyEvolutions()
    block()
    unapplyEvolutions()
  }
}
