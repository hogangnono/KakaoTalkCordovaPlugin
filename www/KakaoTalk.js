var exec = require('cordova/exec');

var KakaoTalk = {
    /** 카카오톡으로 로그인 시도, 카카오톡 사용 불가 시 계정으로 로그인 시도 */
    login: function (successCallback, errorCallback) {
        exec(successCallback, errorCallback, "KakaoTalk", "login", []);
    },
    /** 카카오 계정으로 로그인 시도 */
    loginWithAccount: function (successCallback, errorCallback) {
        exec(successCallback, errorCallback, "KakaoTalk", "loginWithAccount", []);
    },
    logout: function (successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'KakaoTalk', 'logout', []);
    },
    /** iOS only */
    loginCallback: function (url, successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'KakaoTalk', 'loginCallback', [ url ]);
    },
    isAvailable: function () {
        return new Promise(function(resolve) {
            exec(function(result) {
                resolve(result === "success" ? true : false)
            }, function() {
                resolve(false)
            }, 'KakaoTalk', 'isAvailable', []);
        });
    }
};

module.exports = KakaoTalk;
