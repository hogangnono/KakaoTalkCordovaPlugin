import KakaoSDKCommon
import KakaoSDKUser
import KakaoSDKAuth

@objc(KakaoTalk)
class KakaoTalk: CDVPlugin {

    override func pluginInitialize() {
        if let infoDict = Bundle.main.infoDictionary,
           let kakaoAppKey = infoDict["KAKAO_APP_KEY"] as? String {
            KakaoSDK.initSDK(appKey: kakaoAppKey)
            
            NotificationCenter.default.addObserver(self, selector: #selector(self.applicationLaunchedWithUrl(notification:)), name: .CDVPluginHandleOpenURL, object: nil)
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

    @objc(isAvailable:)
    func isAvailable(command: CDVInvokedUrlCommand) {
        self.commandDelegate.send(CDVPluginResult(status: .ok, messageAs: UserApi.isKakaoTalkLoginAvailable() ? "success" : "fail"), callbackId: command.callbackId)
    }
    
    @objc(applicationLaunchedWithUrl:)
    func applicationLaunchedWithUrl(notification: Notification) {
        guard let url = notification.object as? URL else {
            return
        }
        
        guard AuthApi.isKakaoTalkLoginUrl(url) else {
            return
        }
        
        _ = AuthController.handleOpenUrl(url: url)
    }
}
