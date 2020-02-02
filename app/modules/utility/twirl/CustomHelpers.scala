package modules.utility.twirl

import views.html

object CustomHelpers {

  import views.html.helper.FieldConstructor
  implicit val myFields = FieldConstructor(html.component.customFieldConstructorTemplate.f)

  val AppName = "play-scala-opinionated-backend-seed"
}
