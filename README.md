KakaoTalk Cordova Plugin
========================

A plugman compatible Cordova plugin for the KakaoTalk(https://developers.kakao.com)

Make sure you've registered your app with Kakao and to have an KAKAO_APP_KEY

Cordova Install Note:
========================

__Android

nothing to do ;-)
But the Android app must register key hash(https://developers.kakao.com/docs/android#getting-started-launch-sample-app)


__iOS

1. Install Kakao SDK (https://developers.kakao.com/docs/ios)
2. Add following code to appDelegate

```
#import <KakaoOpenSDK/KakaoOpenSDK.h>

- (BOOL)application:(UIApplication *)application openURL:(NSURL *)url
                                       sourceApplication:(NSString *)sourceApplication
                                              annotation:(id)annotation {

    ...
    if ([KOSession isKakaoAccountLoginCallback:url]){return [KOSession handleOpenURL:url];}
    ...
    
}

- (void)applicationDidBecomeActive:(UIApplication *)application{[KOSession handleDidBecomeActive];}
```

cordova plugin add https://github.com/lihak/KakaoTalkCordovaPlugin --variable KAKAO_APP_KEY=%KAKAO_APP_KEY%


How to use the plugin
========================

### Usage

This plugin adds an object to the window. Right now, you can login, logout and check app installed.

#### Login

Login using the `.login` method:
```
KakaoTalk.login(
    function (result) { // success
        console.log('Successful login!');
		console.log(result);
    },
    function (message) { // error
        console.log('Error logging in');
		console.log(message);
    }
);
```

The login reponse object is defined as:
```
{
  id: '<KakaoTalk User Id>',
  nickname: '<KakaoTalk User Nickname>',
  profile_image: '<KakaoTalk User ProfileImage>'
}
```

#### Logout

Logout using the `.logout` method:
```
Kakaotalk.logout(
	function() {
		console.log('Successful logout!');
	}, function() {
		console.log('Error logging out');
	}
);
```

#### loginCallback

OpenURL method can only triggered once below iOS 10. so you have to workaround this situation with `loginCallback` method. It'll do same with the codes you defined on appDelegate.

You should have ready to use custom URL Scheme. (You can check out it `cordova-plugin-customurlscheme`)

```
window.handleOpenURL = function (url) {
  if (url.indexOf('kakao') === 0) {
    Kakaotalk.loginCallback(url);
  }
}
```

If you use other platform except iOS, you can use `device.platform` value.

```
window.handleOpenURL = function (url) {
  if (url.indexOf('kakao') === 0 && typeof device !== 'undefined' && device.platform.toLowerCase() === 'ios') {
    Kakaotalk.loginCallback(url);
  }
}
```

#### isAvailable

isAvailable using the `.isAvailable` method:
```
const result = await Kakaotalk.isAvailable()
```

The isAvailable response type is boolean.
