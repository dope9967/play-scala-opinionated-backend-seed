package utility.application

import com.dimafeng.testcontainers.PostgreSQLContainer
import org.scalatest.mockito.MockitoSugar
import play.api.{Application, Configuration, Mode}
import play.api.inject.guice.GuiceApplicationBuilder

object TestApplications extends MockitoSugar {

  def basicDatabaseTestApplication(container: PostgreSQLContainer): Application = {
    val configuration: Configuration = Configuration.from(
      Map(
        "slick.dbs.default.profile" -> "modules.utility.database.ExtendedPostgresProfile$",
        "slick.dbs.default.db.url" -> container.jdbcUrl,
        "slick.dbs.default.db.user" -> container.username,
        "slick.dbs.default.db.password" -> container.password
      )
    )

    GuiceApplicationBuilder(configuration = configuration)
      .in(Mode.Test)
      .build()
  }
}
