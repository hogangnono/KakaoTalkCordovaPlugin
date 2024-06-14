# KakaoTalk Cordova Plugin

A plugman compatible Cordova plugin for the KakaoTalk(https://developers.kakao.com)

Make sure you've registered your app with Kakao and to have an KAKAO_APP_KEY

# Cordova Install Note:

cordova plugin add https://github.com/hogangnono/KakaoTalkCordovaPlugin --variable KAKAO_APP_KEY=%KAKAO_APP_KEY%

Android app must register key hash(https://developers.kakao.com/docs/android#getting-started-launch-sample-app)

# How to use the plugin

### Usage

This plugin adds an object to the window. Right now, you can login, logout and check app installed.

#### Login

Login with KakaoTalk(If not available, with Kakao account) using the `.login` method:

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

#### LoginWithAccount

Login with Kakao account using the `.loginWithAccount` method:

```
KakaoTalk.loginWithAccount(
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

#### isAvailable

isAvailable using the `.isAvailable` method:

```
const result = await Kakaotalk.isAvailable()
```

The isAvailable response type is boolean.
