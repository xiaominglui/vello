
package com.mili.xiaominglui.app.vello.ui;

import android.accounts.Account;
import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.WindowManager;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.github.johnpersano.supertoasts.SuperToast;
import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.authenticator.Constants;
import com.mili.xiaominglui.app.vello.config.VelloConfig;
import com.mili.xiaominglui.app.vello.util.AccountUtils;
import com.mili.xiaominglui.app.vello.util.AccountUtils.AuthenticateCallback;

public class AccountActivity extends SherlockFragmentActivity implements LoginFragment.onButtonClickedListener, AuthenticateCallback {

    public static final String EXTRA_FINISH_INTENT = "com.mili.xiaominglui.app.vello.extra.FINISH_INTENT";

    private static final int REQUEST_AUTHENTICATE = 100;
    private Account mMyTrelloAccount;
    private Intent mFinishIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isFinishing()) {
        	return;
        }

        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }
        }
        setContentView(R.layout.activity_account);

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().add(R.id.fragment_container_master, new LoginFragment(), "welcome").commit();
        
        if (getIntent().hasExtra(EXTRA_FINISH_INTENT)) {
            mFinishIntent = getIntent().getParcelableExtra(EXTRA_FINISH_INTENT);
        }
    }

    private void tryAuthenticate() {
        AccountUtils.authAndAddTrelloAccount(AccountActivity.this, AccountActivity.this, REQUEST_AUTHENTICATE, mMyTrelloAccount);
    }

    @Override
    public void onAuthenticated(String username) {
        if (mFinishIntent != null) {
            mFinishIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            mFinishIntent.setAction(Intent.ACTION_MAIN);
            mFinishIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(mFinishIntent);
        }
        finish();
    }

    @Override
    public void onOperationCanceled() {

    }

    @Override
	public void onSignInButtonClicked() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork == null || !activeNetwork.isConnected()) {
			SuperToast superToast = new SuperToast(getApplicationContext());
			superToast.setDuration(SuperToast.DURATION_SHORT);
			superToast.setBackgroundResource(SuperToast.BACKGROUND_REDTRANSLUCENT);
			superToast.setTextColor(Color.WHITE);
			superToast.setText(getString(R.string.toast_no_connection));
			superToast.show();
            return;
        }
		mMyTrelloAccount = new Account(VelloConfig.TRELLO_DEFAULT_ACCOUNT_NAME, Constants.ACCOUNT_TYPE);
        tryAuthenticate();
	}
}
