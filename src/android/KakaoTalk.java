package com.hogangnono.plugin.kakao;

import android.app.Activity;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hogangnono.hogangnono.R;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.common.KakaoSdk;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class KakaoTalk extends CordovaPlugin {

    private static final String LOG_TAG = "KakaoTalk";
    private static volatile Activity currentActivity;
    private static boolean initialized = false;
    private CallbackContext callback;

    /**
     * Initialize cordova plugin kakaotalk
     *
     * @param cordova
     * @param webView
     */
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        Log.v(LOG_TAG, "kakao : initialize");
        super.initialize(cordova, webView);
        currentActivity = this.cordova.getActivity();
        if (!initialized) {
            Log.d(LOG_TAG, "kakao app key : " + currentActivity.getString(R.string.kakao_app_key));
            KakaoSdk.init(currentActivity.getApplicationContext(), currentActivity.getString(R.string.kakao_app_key));
            initialized = true;
        }
    }

    /**
     * Execute plugin
     *
     * @param action
     * @param args
     * @param callbackContext
     */
    public boolean execute(final String action, JSONArray args, final CallbackContext callbackContext)
            throws JSONException {
        Log.v(LOG_TAG, "kakao : execute " + action);
        cordova.setActivityResultCallback(this);
        callback = callbackContext;
        if (action.equals("login")) {
            this.login();
            return true;
        }
        if (action.equals("loginWithAccount")) {
            this.loginWithAccount();
            return true;
        }
        if (action.equals("logout")) {
            this.logout();
            return true;
        }
        if (action.equals("isAvailable")) {
            this.isAvailable();
            return true;
        }
        return false;
    }

    /**
     * Log in
     */
    private void login() {
        currentActivity.runOnUiThread(new Runnable() {
            public void run() {
                if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(currentActivity.getApplicationContext())) {
                    UserApiClient.getInstance().loginWithKakaoTalk(currentActivity, loginCallback);
                } else {
                    UserApiClient.getInstance().loginWithKakaoAccount(currentActivity, loginCallback);
                }
            }
        });
    }


    /**
     * Log in with account
     */
    private void loginWithAccount() {
        currentActivity.runOnUiThread(new Runnable() {
            public void run() {
                UserApiClient.getInstance().loginWithKakaoAccount(currentActivity, loginCallback);
            }
        });
    }

    Function2<OAuthToken, Throwable, Unit> loginCallback = (token, loginError) -> {
        if (loginError != null) {
            Log.e(LOG_TAG, "로그인 실패", loginError);
        } else {
            Log.i(LOG_TAG, "로그인 성공(token) : " + token.getAccessToken());
            UserApiClient.getInstance().me((user, meError) -> {
                if (meError != null) {
                    Log.e(LOG_TAG, "사용자 정보 요청 실패", meError);
                } else {
                    Log.i(LOG_TAG, "사용자 정보 요청 성공(id) : " + user.getId());
                    callback.success(handleResult(user, token));
                }
                return null;
            });
        }
        return null;
    };

    /**
     * Log out
     */
    private void logout() {
        currentActivity.runOnUiThread(new Runnable() {
            public void run() {
                UserApiClient.getInstance().logout((error) -> {
                    if (error != null) {
                        Log.e(LOG_TAG, "로그아웃 실패", error);
                    } else {
                        Log.i(LOG_TAG, "로그아웃 성공");
                    }
                    callback.success();
                    return null;
                });
            }
        });
    }

    /**
     * is Available (exist kakaotalk app)
     */
    private void isAvailable() {
        currentActivity.runOnUiThread(new Runnable() {
            public void run() {
                boolean available = UserApiClient.getInstance()
                        .isKakaoTalkLoginAvailable(currentActivity.getApplicationContext());
                Log.d(LOG_TAG, "kakaoTalk : isAvailable = " + available);
                if (available) {
                    callback.success("success");
                } else {
                    callback.success("fail");
                }
            }
        });
    }

    /**
     * Result
     *
     * @param user
     * @param token
     */
    private JSONObject handleResult(User user, OAuthToken token) {
        Log.v(LOG_TAG, "kakao : handleResult");
        JSONObject response = new JSONObject();
        try {
            response.put("id", user.getId());
            response.put("nickname", user.getKakaoAccount().getProfile().getNickname());
            response.put("profile_image", user.getKakaoAccount().getProfile().getProfileImageUrl());
            response.put("access_token", token.getAccessToken());
            Log.i(LOG_TAG, "handleResult : " + response.toString());
        } catch (JSONException e) {
            Log.v(LOG_TAG, "kakao : handleResult error - " + e.toString());
        }
        return response;
    }
}
