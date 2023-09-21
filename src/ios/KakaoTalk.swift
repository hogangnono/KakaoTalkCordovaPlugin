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
        let loginCompletion = createLoginCompletion(command: command)
        
        if UserApi.isKakaoTalkLoginAvailable() {
            UserApi.shared.loginWithKakaoTalk(completion: loginCompletion)
        } else {
            UserApi.shared.loginWithKakaoAccount(completion: loginCompletion)
        }
    }

    @objc(loginWithAccount:)
    func loginWithAccount(command: CDVInvokedUrlCommand) {
        UserApi.shared.loginWithKakaoAccount(completion: createLoginCompletion(command: command))
    }

    private func createLoginCompletion(command: CDVInvokedUrlCommand) -> ((OAuthToken?, Error?) -> Void) {
        return { oauthToken, error in
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
    }

    @objc(logout:)
    func logout(command: CDVInvokedUrlCommand) {
        UserApi.shared.logout { error in
            guard error == nil else {
                self.commandDelegate.send(CDVPluginResult(status: .error, messageAs: error!.localizedDescription), callbackId: command.callbackId)
                return
            }
            
            self.commandDelegate.send(CDVPluginResult(status: .ok), callbackId: command.callbackId)
        }
    }
    
    @objc(loginCallback:)
    func loginCallback(command: CDVInvokedUrlCommand) {        
        guard let urlString = command.arguments[0] as? String,
              let url = URL(string: urlString) else {
            self.commandDelegate.send(CDVPluginResult(status: .error, messageAs: "Invalid url"), callbackId: command.callbackId)
            return
        }
        
        guard AuthApi.isKakaoTalkLoginUrl(url) else {
            self.commandDelegate.send(CDVPluginResult(status: .error, messageAs: "isKakaoTalkLoginUrl fail"), callbackId: command.callbackId)
            return
        }
        
        guard AuthController.handleOpenUrl(url: url) else {
            self.commandDelegate.send(CDVPluginResult(status: .error, messageAs: "handleOpenUrl fail"), callbackId: command.callbackId)
            return
        }
        
        self.commandDelegate.send(CDVPluginResult(status: .ok), callbackId: command.callbackId)
    }

    @objc(isAvailable:)
    func isAvailable(command: CDVInvokedUrlCommand) {
        self.commandDelegate.send(CDVPluginResult(status: .ok, messageAs: UserApi.isKakaoTalkLoginAvailable() ? "success" : "fail"), callbackId: command.callbackId)
    }
}
