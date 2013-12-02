
package com.mili.xiaominglui.app.vello.util;

import java.io.IOException;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TrelloApi;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.avos.avoscloud.ParseInstallation;
import com.avos.avoscloud.PushService;
import com.mili.xiaominglui.app.vello.authenticator.Constants;
import com.mili.xiaominglui.app.vello.config.VelloConfig;
import com.mili.xiaominglui.app.vello.data.model.WordCard;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent;
import com.mili.xiaominglui.app.vello.data.provider.VelloProvider;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbWordCard;
import com.mili.xiaominglui.app.vello.ui.AccountActivity;

public class AccountUtils {
    private static final String TAG = AccountUtils.class.getSimpleName();

    private static final String PREF_CHOSEN_ACCOUNT = "chosen_account";
    private static final String PREF_AUTH_TOKEN = "auth_token";
    private static final String PREF_VOCABULARY_BOARD_ID = "board_id";
    private static final String PREF_VOCABULARY_BOARD_WEB_HOOK_ID = "board_hook_id";
    private static final String PREF_VOCABULARY_BOARD_NAME = "board_name";
    private static final String PREF_VOCABULARY_BOARD_NAME_DEFAULT = "MyWords";
    private static final String PREF_VOCABULARY_BOARD_VERIFICATION_STRING = "TrelloDB";

    // private static final String[] PREF_VOCABULARY_LIST_ID = {"new", "1st",
    // "2nd", "3rd", "4th", "5th", "6th", "7th", "8th"};
    public static final String[] VOCABULARY_LISTS_TITLE_ID = {
            "new", "1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th"
    };

    public static boolean hasVocabularyBoard(final Context context) {
        return !TextUtils.isEmpty(getVocabularyBoardId(context));
    }

    public static boolean isVocabularyBoardWellFormed(final Context context) {
        for (int i = 0; i < VOCABULARY_LISTS_TITLE_ID.length; i++) {
            if (TextUtils.isEmpty(getVocabularyListId(context, i))) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean isWebHooksCreated(final Context context) {
    	return !TextUtils.isEmpty(getVocabularyBoardWebHookId(context));
    }

    public static boolean isAuthenticated(final Context context) {
        return !TextUtils.isEmpty(getChosenAccountName(context));
    }

    public static void startAuthenticationFlow(final Context context,
            final Intent finishIntent) {
        Intent loginFlowIntent = new Intent(context, AccountActivity.class);
        loginFlowIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        loginFlowIntent.putExtra(AccountActivity.EXTRA_FINISH_INTENT,
                finishIntent);
        context.startActivity(loginFlowIntent);
    }
    
    public static interface AuthenticateCallback {
        public boolean shouldCancelAuthentication();

        public void onAuthTokenAvailable(String authToken);
    }

    public static void addTrelloAccount(Activity activity,
            AuthenticateCallback callback, int activityRequestCode,
            Account account) {
        AccountManager.get(activity).addAccount(
                account.type,
                Constants.AUTHTOKEN_TYPE,
                null,
                null,
                activity,
                getAccountManagerCallback(callback, account, activity,
                        activity, activityRequestCode), null);

    }

    private static AccountManagerCallback<Bundle> getAccountManagerCallback(
            final AuthenticateCallback callback, final Account account,
            final Context context, final Activity activity,
            final int activityRequestCode) {
        return new AccountManagerCallback<Bundle>() {
            public void run(AccountManagerFuture<Bundle> future) {
                if (callback != null && callback.shouldCancelAuthentication()) {
                    return;
                }

                try {
                    Bundle bundle = future.getResult();

                    if (activity != null && bundle.containsKey(AccountManager.KEY_INTENT)) {
                        Intent intent = bundle.getParcelable(AccountManager.KEY_INTENT);
                        intent.setFlags(intent.getFlags() & ~Intent.FLAG_ACTIVITY_NEW_TASK);
                        activity.startActivityForResult(intent, activityRequestCode);

                    } else if (bundle.containsKey(AccountManager.KEY_AUTHTOKEN)) {
                        final String token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
//                        addAccount(context, name, type, token);
//                        setAuthToken(context, token);
//                        setChosenAccountName(context, name);
                        if (callback != null) {
                            callback.onAuthTokenAvailable(token);
                        }
                    }
                } catch (OperationCanceledException e) {
                    Log.e(TAG, "Authentication error " + e);
                } catch (AuthenticatorException e) {
                	Log.e(TAG, "Authentication error " + e);
				} catch (IOException e) {
					Log.e(TAG, "Authentication error " + e);
				}
            }
        };
    }
    
    public static String getVocabularyBoardWebHookId(final Context context) {
    	SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
    	return sp.getString(PREF_VOCABULARY_BOARD_WEB_HOOK_ID, "");
    }
    
    public static void setVocabularyBoardWebHookId(final Context context, String hookId) {
    	SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
    	sp.edit().putString(PREF_VOCABULARY_BOARD_WEB_HOOK_ID, hookId).commit();
    }
    
    public static String getVocabularyBoardId(final Context context) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sp.getString(PREF_VOCABULARY_BOARD_ID, "");
    }

    public static void setVocabularyBoardId(final Context context, final String id) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        sp.edit().putString(PREF_VOCABULARY_BOARD_ID, id).commit();
    }

    public static String getVocabularyBoardName(final Context context) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sp.getString(PREF_VOCABULARY_BOARD_NAME, PREF_VOCABULARY_BOARD_NAME_DEFAULT);
    }

    public static String getVocabularyBoardVerification() {
        return PREF_VOCABULARY_BOARD_VERIFICATION_STRING;
    }

    public static String getChosenAccountName(final Context context) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sp.getString(PREF_CHOSEN_ACCOUNT, "");
    }

    public static String getVocabularyListId(final Context context, final int position) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sp.getString(VOCABULARY_LISTS_TITLE_ID[position], "");
    }

    public static void setVocabularyListId(final Context context, final String id,
            final int position) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        sp.edit().putString(VOCABULARY_LISTS_TITLE_ID[position], id).commit();
    }

    public static String getVocabularyListTitle(final int position) {
        return VOCABULARY_LISTS_TITLE_ID[position];
    }

    public static int getVocabularyListPosition(final Context context, final String idList) {
        for (int i = 0; i < VOCABULARY_LISTS_TITLE_ID.length; i++) {
            if (getVocabularyListId(context, i).equals(idList)) {
                return i;
            }
        }
        return 0;
    }

    public static void setChosenAccountName(final Context context,
            final String accountName) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        sp.edit().putString(PREF_CHOSEN_ACCOUNT, accountName).commit();
    }

    public static void addAccount(final Context context, final String name,
            final String type, final String token) {
        AccountManager am = AccountManager.get(context);
        final Account account = new Account(name, type);
        am.addAccountExplicitly(account, token, null);
    }
    
    public static String getAuthToken(final Context context) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sp.getString(PREF_AUTH_TOKEN, "");
    }

    public static void setAuthToken(final Context context,
            final String authToken) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        sp.edit().putString(PREF_AUTH_TOKEN, authToken).commit();
    }

    public static void invalidateAuthToken(final Context context) {
        AccountManager am = AccountManager.get(context);
        am.invalidateAuthToken(Constants.ACCOUNT_TYPE,
                getAuthToken(context));
        setAuthToken(context, null);
    }

    public static void signOut(final Context context) {
        invalidateAuthToken(context);
        
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        sp.edit().clear().commit();
        
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();

		context.getContentResolver().delete(
				VelloContent.CONTENT_URI, null, null);
    }
}
