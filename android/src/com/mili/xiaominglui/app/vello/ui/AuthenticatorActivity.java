
package com.mili.xiaominglui.app.vello.ui;

import org.scribe.builder.ServiceBuilder;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.authenticator.Constants;
import com.mili.xiaominglui.app.vello.authenticator.TrelloAuthApi;
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
    private OAuthService mService;
    private Token mRequestToken;
    private ProgressBar mProgressBar;


	private class MyWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (url.contains("&oauth_verifier=")) {
				String verifier = (url.split("&oauth_verifier="))[1];
				if (verifier != null && verifier.length() == Constants.VERIFIER_LENGTH) {
					if (VelloConfig.DEBUG_SWITCH) {
						Log.i(TAG, "got verifier, next step...");
					}
					new GetTrelloAccessTokenTask().execute(verifier);
				} else {
					if (VelloConfig.DEBUG_SWITCH) {
						Log.i(TAG, "trello not response verifier");
					}
					finishFailure();
				}
				return true;
			} else if (url.equals("https://trello.com/")) {
				// user touch the Deny button
				if (VelloConfig.DEBUG_SWITCH) {
					Log.i(TAG, "user deny auth");
				}
				finishFailure();
				return true;
			} else {
				if (VelloConfig.DEBUG_SWITCH) {
					Log.i(TAG, url + " ---- loading...");
				}
				view.loadUrl(url);
				return true;
			}
		}
		
		@Override
		public void onPageFinished(WebView view, String url) {
			if (mProgressBar.isShown()) {
				mProgressBar.setVisibility(View.INVISIBLE);
				mWebView.setVisibility(View.VISIBLE);
			}
		}
		
		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			finishFailure();
		}
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_oauth);
    	
    	mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.setWebViewClient(new MyWebViewClient());
        new GetTrelloAuthVerifierStringTask().execute();
    }
    
    @Override
    protected void onDestroy() {
    	mWebView.loadUrl("about:blank");
    	mWebView.clearCache(true);
    	super.onDestroy();
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
    private void finishAuthenticated(Token authToken) {

        Log.i(TAG, "finishAuthenticated()");
//        AccountUtils.addAccount(getApplicationContext(), VelloConfig.TRELLO_DEFAULT_ACCOUNT_NAME, Constants.ACCOUNT_TYPE, authToken);
//        AccountUtils.setAuthToken(getApplicationContext(), authToken);
//        AccountUtils.setChosenAccountName(getApplicationContext(), VelloConfig.TRELLO_DEFAULT_ACCOUNT_NAME);
        final Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, VelloConfig.TRELLO_DEFAULT_ACCOUNT_NAME);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
        intent.putExtra(AccountManager.KEY_AUTHTOKEN, authToken);
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
    
    class GetTrelloAuthVerifierStringTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			mService = new ServiceBuilder()
					.provider(TrelloAuthApi.class)
					.apiKey(VelloConfig.API_KEY)
					.apiSecret(VelloConfig.API_SECRET)
					.build();
			mRequestToken = mService.getRequestToken();
			
			return mService.getAuthorizationUrl(mRequestToken);
		}
		
		@Override
		protected void onPostExecute(String result) {
			mWebView.loadUrl(result);
		}
    	
    }
    
    class GetTrelloAccessTokenTask extends AsyncTask<String, Void, Token> {

		@Override
		protected Token doInBackground(String... params) {
			Verifier verifier = new Verifier(params[0]);
			Token accessToken = mService.getAccessToken(mRequestToken, verifier);
			return accessToken;
		}
		
		@Override
		protected void onPostExecute(Token token) {
			mWebView.loadUrl("about:blank");
			finishAuthenticated(token);
		}
    }
}
