var exec = require('cordova/exec');

var KakaoTalk = {
    login: function (successCallback, errorCallback) {
        exec(successCallback, errorCallback, "KakaoTalk", "login", []);
    },
    logout: function (successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'KakaoTalk', 'logout', []);
    },
    loginCallback: function (url, successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'KakaoTalk', 'loginCallback', [ url ]);
    }
};

module.exports = KakaoTalk;
