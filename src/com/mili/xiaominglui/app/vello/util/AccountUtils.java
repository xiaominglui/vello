package com.mili.xiaominglui.app.vello.util;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.mili.xiaominglui.app.vello.authenticator.Constants;
import com.mili.xiaominglui.app.vello.ui.AccountActivity;

public class AccountUtils {
    private static final String TAG = AccountUtils.class.getSimpleName();

    private static final String PREF_CHOSEN_ACCOUNT = "chosen_account";
    private static final String PREF_AUTH_TOKEN = "auth_token";

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

		    if (activity != null
			    && bundle.containsKey(AccountManager.KEY_INTENT)) {
			Intent intent = bundle
				.getParcelable(AccountManager.KEY_INTENT);
			intent.setFlags(intent.getFlags()
				& ~Intent.FLAG_ACTIVITY_NEW_TASK);
			activity.startActivityForResult(intent,
				activityRequestCode);

		    } else if (bundle.containsKey(AccountManager.KEY_PASSWORD)) {
			final String name = bundle
				.getString(AccountManager.KEY_ACCOUNT_NAME);
			final String type = bundle
				.getString(AccountManager.KEY_ACCOUNT_TYPE);
			final String token = bundle
				.getString(AccountManager.KEY_PASSWORD);
			addAccount(context, name, type, token);
			setAuthToken(context, token);
			setChosenAccountName(context, account.name);
			if (callback != null) {
			    callback.onAuthTokenAvailable(token);
			}
		    }
		} catch (Exception e) {
		    Log.e(TAG, "Authentication error" + e);
		}
	    }
	};
    }
    
    public static String getChosenAccountName(final Context context) {
	SharedPreferences sp = PreferenceManager
		.getDefaultSharedPreferences(context);
	return sp.getString(PREF_CHOSEN_ACCOUNT, null);
    }

    private static void setChosenAccountName(final Context context,
	    final String accountName) {
	SharedPreferences sp = PreferenceManager
		.getDefaultSharedPreferences(context);
	sp.edit().putString(PREF_CHOSEN_ACCOUNT, accountName).commit();
    }
    
    private static void addAccount(final Context context, final String name,
	    final String type, final String token) {
	AccountManager am = AccountManager.get(context);
	final Account account = new Account(name, type);
	am.addAccountExplicitly(account, token, null);
	// Tells the content provider that it can sync this account.
	ContentResolver.setIsSyncable(account, "com.trello", 1);
	ContentResolver.setSyncAutomatically(account, "com.trello", true);
    }
    
    public static String getAuthToken(final Context context) {
	SharedPreferences sp = PreferenceManager
		.getDefaultSharedPreferences(context);
	return sp.getString(PREF_AUTH_TOKEN, null);
    }
    
    private static void setAuthToken(final Context context,
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
//	context.getContentResolver().delete(
//		VelloProviderContract.BASE_CONTENT_URI, null, null);
    }
}
