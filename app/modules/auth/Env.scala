package modules.auth

import com.mohiva.play.silhouette.api.Env
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator

/**
  * The default env.
  */
trait DefaultEnv extends Env {
  type I = AuthorizedUser
  type A = CookieAuthenticator
}
