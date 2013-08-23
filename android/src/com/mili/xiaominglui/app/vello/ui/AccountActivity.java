
package com.mili.xiaominglui.app.vello.ui;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.adapter.TestFragmentAdapter;
import com.mili.xiaominglui.app.vello.authenticator.Constants;
import com.mili.xiaominglui.app.vello.config.VelloConfig;
import com.mili.xiaominglui.app.vello.data.provider.VelloProvider;
import com.mili.xiaominglui.app.vello.util.AccountUtils;
import com.mili.xiaominglui.app.vello.util.AccountUtils.AuthenticateCallback;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.UnderlinePageIndicator;

public class AccountActivity extends SherlockFragmentActivity implements
        OnClickListener, AuthenticateCallback {
    private static final String TAG = AccountActivity.class.getSimpleName();

    public static final String EXTRA_FINISH_INTENT = "com.mili.xiaominglui.app.vello.extra.FINISH_INTENT";

    private static final int REQUEST_AUTHENTICATE = 100;
    private Account mMyTrelloAccount;
    private Intent mFinishIntent;
    private boolean mCancelAuth = false;

    TestFragmentAdapter mAdapter;
    ViewPager mPager;
    PageIndicator mIndicator;

    Button mLogInButton;
    Button mSignUpButton;
    
    /**
     * Sync period in seconds, currently every 8 hours
     */
    private static final long SYNC_PERIOD = 8L * 60L * 60L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_account);

        mAdapter = new TestFragmentAdapter(getSupportFragmentManager());

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        mIndicator = (UnderlinePageIndicator) findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);

        mLogInButton = (Button) findViewById(R.id.log_in);
        mLogInButton.setOnClickListener(this);
        mSignUpButton = (Button) findViewById(R.id.sign_up);
        mSignUpButton.setOnClickListener(this);

        if (getIntent().hasExtra(EXTRA_FINISH_INTENT)) {
            mFinishIntent = getIntent().getParcelableExtra(EXTRA_FINISH_INTENT);
        }
    }

    @Override
    public void onClick(View v) {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork == null || !activeNetwork.isConnected()) {
            Toast.makeText(getApplicationContext(),
                    R.string.no_connection_cant_login, Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        int id = v.getId();
        switch (id) {
            case R.id.log_in:
                mMyTrelloAccount = new Account(VelloConfig.TRELLO_DEFAULT_ACCOUNT_NAME, Constants.ACCOUNT_TYPE);
//                getSupportFragmentManager()
//                        .beginTransaction()
//                        .replace(R.id.fragment_container,
//                                new AuthProgressFragment(), "loading")
//                        .addToBackStack("log_in").commit();
                tryAuthenticate();
                break;
            case R.id.sign_up:
                break;
        }

    }

    private void tryAuthenticate() {
        AccountUtils.addTrelloAccount(AccountActivity.this,
                AccountActivity.this, REQUEST_AUTHENTICATE, mMyTrelloAccount);
    }

    /**
     * This fragment shows a login progress spinner. Upon reaching a timeout of
     * 7 seconds (in case of a poor network connection), the user can try again.
     */
    public static class AuthProgressFragment extends SherlockFragment {
        private static final int TRY_AGAIN_DELAY_MILLIS = 7 * 1000; // 7 seconds

        private final Handler mHandler = new Handler();

        public AuthProgressFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            ViewGroup rootView = (ViewGroup) inflater.inflate(
                    R.layout.fragment_login_loading, container, false);

            final View takingAWhilePanel = rootView
                    .findViewById(R.id.taking_a_while_panel);
            final View tryAgainButton = rootView
                    .findViewById(R.id.retry_button);
            tryAgainButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getFragmentManager().popBackStack();
                }
            });

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isAdded()) {
                        return;
                    }

                    takingAWhilePanel.setVisibility(View.VISIBLE);
                }
            }, TRY_AGAIN_DELAY_MILLIS);

            return rootView;
        }

        @Override
        public void onDetach() {
            super.onDetach();
            ((AccountActivity) getActivity()).mCancelAuth = true;
        }
    }

    @Override
    public boolean shouldCancelAuthentication() {
        return mCancelAuth;
    }

    @Override
    public void onAuthTokenAvailable(String authToken) {
		if (mFinishIntent != null) {
			mFinishIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			mFinishIntent.setAction(Intent.ACTION_MAIN);
			mFinishIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(mFinishIntent);
		}
		finish();
    }
}
