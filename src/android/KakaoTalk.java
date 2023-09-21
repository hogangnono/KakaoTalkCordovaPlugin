package com.hogangnono.plugin.kakao;

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

import com.hogangnono.hogangnono.R;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.auth.model.Prompt;
import com.kakao.sdk.common.KakaoSdk;
import com.kakao.sdk.common.util.Utility;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

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
import java.util.Arrays;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

enum AuthType {
    /**
     * Kakaotalk으로 login을 하고 싶을 경우 지정. Webviews are used if not installed.
     */
    KAKAO_TALK(0),

    /**
     * Kakaostory으로 login을 하고 싶을 경우 지정. Webviews are used if not installed.
     */
    KAKAO_STORY(1),

    /**
     * 웹뷰를 통해 카카오 계정연결을 제공하고 싶을 경우 지정.
     */
    KAKAO_ACCOUNT(2),

    /**
     * 모든 로그인방식을 사용하고 싶을때 지정.
     */
    KAKAO_LOGIN_ALL(4),

    /**
     * Kakaotalk으로 login을 하고 싶을 경우 지정.
     * Webviews are not used even if talk is notinstalled.
     */
    KAKAO_TALK_ONLY(5);

    private final int number;

    AuthType(int i) {
        this.number = i;
    }

    public int getNumber() {
        return number;
    }

    public static AuthType valueOf(int number) {
        if (number == KAKAO_TALK.getNumber()) {
            return KAKAO_TALK;
        } else if (number == KAKAO_STORY.getNumber()) {
            return KAKAO_STORY;
        } else if (number == KAKAO_ACCOUNT.getNumber()) {
            return KAKAO_ACCOUNT;
        } else if (number == KAKAO_LOGIN_ALL.getNumber()) {
            return KAKAO_LOGIN_ALL;
        } else {
            return null;
        }
    }
}

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

    /**
     * Return current activity
     */
    public static Activity getCurrentActivity() {
        return currentActivity;
    }

    private static class Item {
        final int textId;
        public final int icon;
        final AuthType authType;

        Item(final int textId, final Integer icon, final AuthType authType) {
            this.textId = textId;
            this.icon = icon;
            this.authType = authType;
        }
    }

    private List<AuthType> getAuthTypes() {
        final List<AuthType> availableAuthTypes = new ArrayList<>();
        if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(currentActivity.getApplicationContext())) {
            availableAuthTypes.add(AuthType.KAKAO_TALK);
        }
        availableAuthTypes.add(AuthType.KAKAO_ACCOUNT);

        return availableAuthTypes;
    }

    private Item[] createAuthItemArray(final List<AuthType> authTypes) {
        final List<Item> itemList = new ArrayList<Item>();
        if (authTypes.contains(AuthType.KAKAO_TALK)) {
            itemList.add(new Item(R.string.com_kakao_kakaotalk_account, R.drawable.talk, AuthType.KAKAO_TALK));
        }
        if (authTypes.contains(AuthType.KAKAO_ACCOUNT)) {
            itemList.add(new Item(R.string.com_kakao_other_kakaoaccount, R.drawable.account, AuthType.KAKAO_ACCOUNT));
        }

        return itemList.toArray(new Item[itemList.size()]);
    }

    private ListAdapter createLoginAdapter(final Item[] authItems) {
        /*
         * 가능한 auth type들을 유저에게 보여주기 위한 준비.
         */
        return new ArrayAdapter<Item>(
                currentActivity,
                android.R.layout.select_dialog_item,
                android.R.id.text1, authItems) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getContext()
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.layout_login_item, parent, false);
                }
                ImageView imageView = (ImageView) convertView.findViewById(R.id.login_method_icon);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    imageView.setImageDrawable(currentActivity.getResources().getDrawable(authItems[position].icon,
                            getContext().getTheme()));
                } else {
                    imageView.setImageDrawable(currentActivity.getResources().getDrawable(authItems[position].icon));
                }
                TextView textView = (TextView) convertView.findViewById(R.id.login_method_text);
                textView.setText(authItems[position].textId);
                return convertView;
            }
        };
    }

    /**
     * 실제로 유저에게 보여질 dialog 객체를 생성한다.
     *
     * @param authItems 가능한 AuthType들의 정보를 담고 있는 Item array
     * @param adapter   Dialog의 list view에 쓰일 adapter
     * @return 로그인 방법들을 팝업으로 보여줄 dialog
     */
    private Dialog createLoginDialog(final Item[] authItems, final ListAdapter adapter) {
        final Dialog dialog = new Dialog(currentActivity, R.style.LoginDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_login_dialog);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setGravity(Gravity.BOTTOM);
        }

        ListView listView = (ListView) dialog.findViewById(R.id.login_list_view);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final AuthType authType = authItems[position].authType;
                if (authType == AuthType.KAKAO_TALK) {
                    UserApiClient.getInstance().loginWithKakaoTalk(currentActivity, (token, loginError) -> {
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
                    });
                } else if (authType == AuthType.KAKAO_ACCOUNT) {
                    UserApiClient.getInstance().loginWithKakaoAccount(currentActivity, (token, loginError) -> {
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
                    });
                }
                dialog.dismiss();
            }
        });

        Button closeButton = (Button) dialog.findViewById(R.id.login_close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        return dialog;
    }

}
