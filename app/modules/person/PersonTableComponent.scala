package modules.person

import modules.utility.database.ExtendedPostgresProfile
import play.api.db.slick.HasDatabaseConfig

trait PersonTableComponent { self: HasDatabaseConfig[ExtendedPostgresProfile] =>
  import profile.api._

  class PersonTable(tag: Tag) extends Table[Person](tag, "persons") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def age = column[Int]("age")

    def * = (id, name, age) <> ((Person.apply _).tupled, Person.unapply)
  }

  val persons = TableQuery[PersonTable]
}
