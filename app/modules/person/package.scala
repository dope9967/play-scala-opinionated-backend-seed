package modules

import play.api.libs.json.Json

package object person {

  case class CreatePersonForm(name: String, age: Int)

  case class Person(id: Long, name: String, age: Int)

  object Person {
    implicit val personFormat = Json.format[Person]
  }
}
