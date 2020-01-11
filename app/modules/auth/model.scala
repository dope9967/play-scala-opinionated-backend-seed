package modules.auth

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.impl.providers.OAuth2Info

case class PasswordData(
    loginInfo: LoginInfo,
    passwordInfo: PasswordInfo
)

case class OAuth2Data(
    loginInfo: LoginInfo,
    OAuth2Info: OAuth2Info
)
