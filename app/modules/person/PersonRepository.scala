package modules.person

import javax.inject.{Inject, Singleton}
import modules.utility.database.ExtendedPostgresProfile
import play.api.Logger
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PersonRepository @Inject()(
    protected val dbConfigProvider: DatabaseConfigProvider
)(
    implicit ec: ExecutionContext
) extends PersonTableComponent with HasDatabaseConfigProvider[ExtendedPostgresProfile] {
  import profile.api._

  private val logger = Logger(this.getClass)

  def list(): Future[Seq[Person]] = db.run {
    persons.result
  }

  def find(id: Long): Future[Option[Person]] = {
    db.run(persons.filter(_.id === id).result.headOption)
  }

  def create(name: String, age: Int): Future[Person] = db.run {
    (persons.map(p => (p.name, p.age))
      returning persons.map(_.id)
      into ((nameAge, id) => Person(id, nameAge._1, nameAge._2))
      ) += (name, age)
  }

  def save(person: Person): Future[Person] = {
    db.run((persons returning persons).insertOrUpdate(person))
      .map {
        case Some(newPerson) =>
          logger.debug(s"Created new person $newPerson")
          newPerson
        case _ =>
          logger.debug(s"Updated existing person $person")
          person
      }
  }
}
