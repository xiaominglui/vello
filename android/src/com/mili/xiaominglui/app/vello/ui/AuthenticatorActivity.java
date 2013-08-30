
package com.mili.xiaominglui.app.vello.ui;

import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.authenticator.Constants;
import com.mili.xiaominglui.app.vello.config.VelloConfig;
import com.mili.xiaominglui.app.vello.util.AccountUtils;

/**
 * Activity which displays login screen to the user.
 */
public class AuthenticatorActivity extends AccountAuthenticatorActivity {
    /** The Intent flag to confirm credentials. */
    public static final String PARAM_CONFIRM_CREDENTIALS = "confirmCredentials";

    /** The Intent extra to store password. */
    public static final String PARAM_PASSWORD = "password";

    /** The Intent extra to store username. */
    public static final String PARAM_USERNAME = "username";

    /** The Intent extra to store username. */
    public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";

    /** The tag used to log to adb console. */
    private static final String TAG = "AuthenticatorActivity";

    private WebView mWebView;


	private class MyWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (VelloConfig.DEBUG_SWITCH) {
				Log.d(TAG, "trello url = " + url);
			}
			if (url.contains("#token=")) {
				String token = (url.split("#token="))[1];
				if (token != null
						&& token.length() == Constants.AUTHTOKEN_LENGTH) {
					finishAuthenticated(token);
				} else {
					if (VelloConfig.DEBUG_SWITCH) {
						Log.i(TAG, "trello not response token");
					}
					finishFailure();
				}
				view.clearCache(true);
				return true;
			} else if (url.equals("https://trello.com/")) {
				// user touch the Deny button
				if (VelloConfig.DEBUG_SWITCH) {
					Log.i(TAG, "user deny auth");
				}
				finishFailure();
				return true;
			} else {
				view.loadUrl(url);
				return true;
			}
		}

	}

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle icicle) {
        final Activity activity = this;
        final ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        super.onCreate(icicle);
        setContentView(R.layout.activity_oauth);
        mWebView = (WebView) findViewById(R.id.webview);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
//        webSettings.setDomStorageEnabled(true);

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                progressDialog.show();
                mWebView.setVisibility(View.INVISIBLE);
                progressDialog.setProgress(0);
                activity.setProgress(progress * 1000);

                progressDialog.incrementProgressBy(progress);

                if (progress == 100 && progressDialog.isShowing()) {
                	progressDialog.dismiss();
                	mWebView.setVisibility(View.VISIBLE);
                }
            }
        });

        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.loadUrl("file:///android_asset/connect.html");

    }
    
    /**
     * Called when response is received from the server for authentication
     * request. See onAuthenticationResult(). Sets the
     * AccountAuthenticatorResult which is sent back to the caller. We store the
     * authToken that's returned from the server as the 'password' for this
     * account - so we're never storing the user's actual password locally.
     * 
     * @param result the confirmCredentials result.
     */
    private void finishAuthenticated(String authToken) {

        Log.i(TAG, "finishAuthenticated()");
        AccountUtils.addAccount(getApplicationContext(), VelloConfig.TRELLO_DEFAULT_ACCOUNT_NAME, Constants.ACCOUNT_TYPE, authToken);
        AccountUtils.setAuthToken(getApplicationContext(), authToken);
        AccountUtils.setChosenAccountName(getApplicationContext(), VelloConfig.TRELLO_DEFAULT_ACCOUNT_NAME);
        final Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, VelloConfig.TRELLO_DEFAULT_ACCOUNT_NAME);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
        intent.putExtra(AccountManager.KEY_PASSWORD, authToken);
        setAccountAuthenticatorResult(intent.getExtras());
        finish();
    }
    
    private void finishFailure() {
    	Log.i(TAG, "finishFailure()");
        final Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_ERROR_CODE, AccountManager.ERROR_CODE_REMOTE_EXCEPTION);
        setAccountAuthenticatorResult(intent.getExtras());
        finish();
    }
}
