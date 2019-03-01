package modules.utility.database

import com.github.tminglei.slickpg._
import slick.basic.Capability

trait ExtendedPostgresProfile extends ExPostgresProfile
  with PgArraySupport
  with PgDate2Support
  with PgRangeSupport
  with PgHStoreSupport
  with PgPlayJsonSupport
  with PgSearchSupport
  with PgNetSupport
  with PgLTreeSupport {

  def pgjson = "jsonb"

  // disable this if issue https://github.com/slick/slick/pull/1983 is still unfixed
  // because insertOrUpdate queries for tables with a primary key composed of all columns will fail
  override protected def computeCapabilities: Set[Capability] =
    super.computeCapabilities + slick.jdbc.JdbcCapabilities.insertOrUpdate

  override val api = new API with ArrayImplicits
    with DateTimeImplicits
    with PlayJsonImplicits
    with NetImplicits
    with LTreeImplicits
    with RangeImplicits
    with HStoreImplicits
    with SearchImplicits
    with SearchAssistants {
  }
}

object ExtendedPostgresProfile extends ExtendedPostgresProfile
