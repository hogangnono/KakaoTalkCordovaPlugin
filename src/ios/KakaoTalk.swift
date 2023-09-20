import KakaoSDKCommon
import KakaoSDKUser
import KakaoSDKAuth

@objc(KakaoTalk)
class KakaoTalk: CDVPlugin {

    override func pluginInitialize() {
        if let infoDict = Bundle.main.infoDictionary,
           let kakaoAppKey = infoDict["KAKAO_APP_KEY"] as? String {
            KakaoSDK.initSDK(appKey: kakaoAppKey)
        }
    }

    @objc(login:)
    func login(command: CDVInvokedUrlCommand) {
        let loginCompletion: (OAuthToken?, Error?) -> Void = { oauthToken, error in
            guard error == nil else {
                self.commandDelegate.send(CDVPluginResult(status: .error, messageAs: error!.localizedDescription), callbackId: command.callbackId)
                return
            }

            guard let oauthToken = oauthToken else {
                self.commandDelegate.send(CDVPluginResult(status: .error, messageAs: "KakaoLogin fail"), callbackId: command.callbackId)
                return
            }

            self.commandDelegate.send(CDVPluginResult(status: .ok, messageAs: ["access_token": oauthToken.accessToken]), callbackId: command.callbackId)
        }

        if(UserApi.isKakaoTalkLoginAvailable()) {
            UserApi.shared.loginWithKakaoTalk(completion: loginCompletion)
        } else {
            UserApi.shared.loginWithKakaoAccount(completion: loginCompletion)
        }
    }

  @objc(logout:)
  func logout(command: CDVInvokedUrlCommand) {
    print("logout")
  }

  @objc(loginCallback:)
  func loginCallback(command: CDVInvokedUrlCommand) {
    print("loginCallback")
  }

  @objc(isAvailable:)
  func isAvailable(command: CDVInvokedUrlCommand) {
    print("isAvailable")
  }
}
