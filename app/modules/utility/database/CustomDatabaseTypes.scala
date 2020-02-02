package modules.utility.database

import play.api.db.slick.HasDatabaseConfig
import play.api.i18n.Lang

trait CustomDatabaseTypes { self: HasDatabaseConfig[ExtendedPostgresProfile] =>
  import profile.api._

  implicit val localeColumnType = MappedColumnType.base[Lang, String](
    { localeType: Lang =>
      localeType.code
    }, { value: String =>
      Lang(value)
    }
  )
}
