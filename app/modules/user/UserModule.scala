package modules.user

import play.api
import play.api.Configuration
import play.api.inject.{Binding, Module}

class UserModule extends Module {

  override def bindings(
      environment: api.Environment,
      configuration: Configuration
  ): Seq[Binding[_]] = {
    Seq(
      bind[UserRepository].to[UserRepositoryImpl]
    )
  }
}
