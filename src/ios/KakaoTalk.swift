@objc(KakaoTalk)
class KakaoTalk: CDVPlugin {

  @objc(login:)
  func login(command: CDVInvokedUrlCommand) {
    print("login")
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
