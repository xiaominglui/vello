package com.mili.xiaominglui.app.vello.ui;

import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.authenticator.Constants;


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

    private String mUsername = "me";
    
    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            
            if (url.contains("#token=")) {
                String token = (url.split("#token="))[1];
                if (token != null && token.length() == Constants.AUTHTOKEN_LENGTH) {
                    Log.d(TAG, "handle the token");
                    finishLogin(token);
                } else {
                    // TODO handle exception
                }
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

        Log.i(TAG, "onCreate(" + icicle + ")");
        super.onCreate(icicle);
        Log.i(TAG, "loading data from Intent");
        requestWindowFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.activity_oauth);
        mWebView = (WebView) findViewById(R.id.webview);
        
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        
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
    private void finishLogin(String authToken) {

        Log.i(TAG, "finishLogin()");
        final Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, mUsername);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
        intent.putExtra(AccountManager.KEY_PASSWORD, authToken);
        setAccountAuthenticatorResult(intent.getExtras());
        finish();
    }
}
