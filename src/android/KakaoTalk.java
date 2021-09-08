package com.lihak.plugin.kakao;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;

import com.kakao.auth.ApprovalType;
import com.kakao.auth.AuthType;
import com.kakao.auth.IApplicationConfig;
import com.kakao.auth.ISessionConfig;
import com.kakao.auth.KakaoAdapter;
import com.kakao.auth.KakaoSDK;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.usermgmt.response.model.UserAccount;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;

public class KakaoTalk extends CordovaPlugin {

    private static final String LOG_TAG = "KakaoTalk";
    private static volatile Activity currentActivity;
    private static boolean initialized = false;
    private SessionCallback callback;

    /**
     * Initialize cordova plugin kakaotalk
     * @param cordova
     * @param webView
     */
    public void initialize(CordovaInterface cordova, CordovaWebView webView)
    {
        Log.v(LOG_TAG, "kakao : initialize");
        super.initialize(cordova, webView);
        currentActivity = this.cordova.getActivity();
        if (!initialized) {
            KakaoSDK.init(new KakaoSDKAdapter());
            initialized = true;
        }
    }

    /**
     * Execute plugin
     * @param action
     * @param args
     * @param callbackContext
     */
    public boolean execute(final String action, JSONArray args, final CallbackContext callbackContext) throws JSONException
    {
        Log.v(LOG_TAG, "kakao : execute " + action);
        cordova.setActivityResultCallback(this);
        callback = new SessionCallback(callbackContext);
        Session.getCurrentSession().addCallback(callback);
        if (action.equals("login")) {
            this.login();
            //requestMe(callbackContext);
            return true;
        }
        if (action.equals("logout")) {
            this.logout(callbackContext);
            return true;
        }
        return false;
    }

    /**
     * Log in
     */
    private void login()
    {
        currentActivity.runOnUiThread(new Runnable() {
            public void run() {
                Session.getCurrentSession().open(AuthType.KAKAO_TALK, currentActivity);
            }
        });
    }

    /**
     * Log out
     * @param callbackContext
     */
    private void logout(final CallbackContext callbackContext)
    {
        currentActivity.runOnUiThread(new Runnable() {
            public void run() {
                UserManagement.getInstance().requestLogout(new LogoutResponseCallback() {
                    @Override
                    public void onCompleteLogout() {
                        callbackContext.success();
                    }
                });
            }
        });
    }

    /**
     * On activity result
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        Log.v(LOG_TAG, "kakao : onActivityResult : " + requestCode + ", code: " + resultCode);
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, intent)) {
            return;
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    /**
     * Result
     * @param result
     */
    private JSONObject handleResult(MeV2Response result)
    {
        Log.v(LOG_TAG, "kakao : handleResult");
        JSONObject response = new JSONObject();
        try {
            response.put("id", result.getId());
            response.put("nickname", result.getNickname());
            response.put("profile_image", result.getProfileImagePath());
            response.put("access_token", Session.getCurrentSession().getTokenInfo().getAccessToken());
        } catch (JSONException e) {
            Log.v(LOG_TAG, "kakao : handleResult error - " + e.toString());
        }
        return response;
    }



    /**
     * Class SessonCallback
     */
    private class SessionCallback implements ISessionCallback {

        private CallbackContext callbackContext;

        public SessionCallback(final CallbackContext callbackContext) {
            this.callbackContext = callbackContext;
        }

        @Override
        public void onSessionOpened() {
            Log.v(LOG_TAG, "kakao : SessionCallback.onSessionOpened");
            UserManagement.getInstance().me(new MeV2ResponseCallback() {
                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                    Log.v(LOG_TAG, "kakao : SessionCallback.onSessionOpened.requestMe.onSessionClosed - " + errorResult);
                    Session.getCurrentSession().checkAndImplicitOpen();
                }

                @Override
                public void onSuccess(MeV2Response result) {
                    callbackContext.success(handleResult(result));
                }
            });
        }

        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            if(exception != null) {
                Log.v(LOG_TAG, "kakao : onSessionOpenFailed" + exception.toString());
            }
        }
    }


    /**
     * Return current activity
     */
    public static Activity getCurrentActivity()
    {
        return currentActivity;
    }

    /**
     * Set current activity
     */
    public static void setCurrentActivity(Activity currentActivity)
    {
        currentActivity = currentActivity;
    }

    /**
     * Class KakaoSDKAdapter
     */
    private static class KakaoSDKAdapter extends KakaoAdapter {

        @Override
        public ISessionConfig getSessionConfig() {
            return new ISessionConfig() {
                @Override
                public AuthType[] getAuthTypes() {
                    return new AuthType[] {AuthType.KAKAO_TALK};
                }

                @Override
                public boolean isUsingWebviewTimer() {
                    return false;
                }

                @Override
                public boolean isSecureMode() {
                    return true;
                }

                @Override
                public ApprovalType getApprovalType() {
                    return ApprovalType.INDIVIDUAL;
                }

                @Override
                public boolean isSaveFormData() {
                    return true;
                }
            };
        }

        @Override
        public IApplicationConfig getApplicationConfig() {
            return new IApplicationConfig() {

                public Activity getTopActivity() {
                    return KakaoTalk.getCurrentActivity();
                }

                @Override
                public Context getApplicationContext() {
                    return KakaoTalk.getCurrentActivity().getApplicationContext();
                }
            };
        }
    }

}
