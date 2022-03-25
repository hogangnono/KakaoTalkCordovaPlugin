package com.lihak.plugin.kakao;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;

import com.kakao.kakaolink.v2.KakaoLinkService;
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

import android.app.Dialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
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
        if (action.equals("isAvailable")) {
            this.isAvailable(callbackContext);
            return true;
        }
        return false;
    }

    private static class Item {
        final int textId;
        public final int icon;
        final int contentDescId;
        final AuthType authType;
        Item(final int textId, final Integer icon, final int contentDescId, final AuthType authType) {
            this.textId = textId;
            this.icon = icon;
            this.contentDescId = contentDescId;
            this.authType = authType;
        }
    }

    private List<AuthType> getAuthTypes() {
        final List<AuthType> availableAuthTypes = new ArrayList<>();

        if (Session.getCurrentSession().getAuthCodeManager().isTalkLoginAvailable()) {
            availableAuthTypes.add(AuthType.KAKAO_TALK);
        }
        availableAuthTypes.add(AuthType.KAKAO_ACCOUNT);

        return availableAuthTypes;
    }

    private Item[] createAuthItemArray(final List<AuthType> authTypes) {
        final List<Item> itemList = new ArrayList<Item>();
        if(authTypes.contains(AuthType.KAKAO_TALK)) {
            itemList.add(new Item(com.kakao.usermgmt.R.string.com_kakao_kakaotalk_account, com.kakao.usermgmt.R.drawable.talk, com.kakao.usermgmt.R.string.com_kakao_kakaotalk_account_tts, AuthType.KAKAO_TALK));
        }
        if(authTypes.contains(AuthType.KAKAO_ACCOUNT)){
            itemList.add(new Item(com.kakao.usermgmt.R.string.com_kakao_other_kakaoaccount, com.kakao.usermgmt.R.drawable.account, com.kakao.usermgmt.R.string.com_kakao_other_kakaoaccount_tts, AuthType.KAKAO_ACCOUNT));
        }

        return itemList.toArray(new Item[itemList.size()]);
    }

    private ListAdapter createLoginAdapter(final Item[] authItems) {
        /*
            가능한 auth type들을 유저에게 보여주기 위한 준비.
        */
        return new ArrayAdapter<Item>(
                currentActivity,
                android.R.layout.select_dialog_item,
                android.R.id.text1, authItems){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getContext()
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(com.kakao.usermgmt.R.layout.layout_login_item, parent, false);
                }
                ImageView imageView = (ImageView) convertView.findViewById(com.kakao.usermgmt.R.id.login_method_icon);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    imageView.setImageDrawable(currentActivity.getResources().getDrawable(authItems[position].icon, getContext().getTheme()));
                } else {
                    imageView.setImageDrawable(currentActivity.getResources().getDrawable(authItems[position].icon));
                }
                TextView textView = (TextView) convertView.findViewById(com.kakao.usermgmt.R.id.login_method_text);
                textView.setText(authItems[position].textId);
                return convertView;
            }
        };
    }

    /**
     * 실제로 유저에게 보여질 dialog 객체를 생성한다.
     * @param authItems 가능한 AuthType들의 정보를 담고 있는 Item array
     * @param adapter Dialog의 list view에 쓰일 adapter
     * @return 로그인 방법들을 팝업으로 보여줄 dialog
     */
    private Dialog createLoginDialog(final Item[] authItems, final ListAdapter adapter) {
        final Dialog dialog = new Dialog(currentActivity, com.kakao.usermgmt.R.style.LoginDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(com.kakao.usermgmt.R.layout.layout_login_dialog);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setGravity(Gravity.BOTTOM);
        }

        ListView listView = (ListView) dialog.findViewById(com.kakao.usermgmt.R.id.login_list_view);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final AuthType authType = authItems[position].authType;
                if (authType != null) {
                    Session.getCurrentSession().open(authType, currentActivity);
                }
                dialog.dismiss();
            }
        });

        Button closeButton = (Button) dialog.findViewById(com.kakao.usermgmt.R.id.login_close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        return dialog;
    }

    /**
     * Log in
     */
    private void login()
    {
        currentActivity.runOnUiThread(new Runnable() {
            public void run() {
                final List<AuthType> authTypes = getAuthTypes();

                if (authTypes.size() == 1) {
                    Session.getCurrentSession().open(authTypes.get(0), currentActivity);
                } else {
                    final Item[] authItems = createAuthItemArray(authTypes);
                    ListAdapter adapter = createLoginAdapter(authItems);
                    final Dialog dialog = createLoginDialog(authItems, adapter);
                    dialog.show();
                }
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
     * is Available
     * @param callbackContext
     */
    private void isAvailable(final CallbackContext callbackContext)
    {
        currentActivity.runOnUiThread(new Runnable() {
            public void run() {
                boolean available = KakaoLinkService.getInstance().isKakaoLinkV2Available(currentActivity.getApplicationContext());
                Log.d(LOG_TAG, "kakaoTalk : isAvailable = " + available);
                if (available) {
                    callbackContext.success("success");
                } else {
                    callbackContext.success("fail");
                }
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
