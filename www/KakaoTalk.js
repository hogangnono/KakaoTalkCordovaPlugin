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
