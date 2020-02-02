package modules.auth

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.impl.providers.OAuth2Info

case class PasswordData(
    loginInfo: LoginInfo,
    passwordInfo: PasswordInfo
)

case class OAuth2Data(
    loginInfo: LoginInfo,
    oauth2Info: OAuth2Info
)

case class UserOAuth2Provider(
    userId: UUID,
    loginInfo: LoginInfo
)
