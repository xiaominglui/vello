package com.mili.xiaominglui.app.vello.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.mili.xiaominglui.app.vello.ui.AccountActivity;


public class AccountUtils {
    private static final String TAG = AccountUtils.class.getSimpleName();
    
    private static final String PREF_CHOSEN_ACCOUNT = "chosen_account";
    
    public static boolean isAuthenticated(final Context context) {
	return !TextUtils.isEmpty(getChosenAccountName(context));
    }
    
    public static String getChosenAccountName(final Context context) {
	SharedPreferences sp = PreferenceManager
		.getDefaultSharedPreferences(context);
	return sp.getString(PREF_CHOSEN_ACCOUNT, null);
    }
    
    public static void startAuthenticationFlow(final Context context, final Intent finishIntent) {
	Intent loginFlowIntent = new Intent(context, AccountActivity.class);
	loginFlowIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	loginFlowIntent.putExtra(AccountActivity.EXTRA_FINISH_INTENT,
		finishIntent);
	context.startActivity(loginFlowIntent);
    }
}
