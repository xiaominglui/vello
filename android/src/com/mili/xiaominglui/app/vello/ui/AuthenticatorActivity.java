
package com.mili.xiaominglui.app.vello.ui;

import java.lang.ref.WeakReference;

import org.scribe.builder.ServiceBuilder;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.authenticator.Constants;
import com.mili.xiaominglui.app.vello.authenticator.TrelloAuthApi;
import com.mili.xiaominglui.app.vello.base.C;
import com.mili.xiaominglui.app.vello.base.log.L;
import com.mili.xiaominglui.app.vello.config.VelloConfig;
import com.mili.xiaominglui.app.vello.service.VelloService;
import com.mili.xiaominglui.app.vello.util.AccountUtils;

public class AuthenticatorActivity extends AccountAuthenticatorActivity {

    /**
     * The Intent extra to store username.
     */
    public static final String PARAM_USERNAME = "username";

    /**
     * The Intent extra to store username.
     */
    public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";

    /**
     * The tag used to log to adb console.
     */
    private static final String TAG = AuthenticatorActivity.class.getSimpleName();
    private static final int AUTH_TIMEOUT_IN_MILLIS = 60 * 1000; // 60 seconds

    private static final int AUTH_FAILURE_NO_VERIFIER = 0;
    private static final int AUTH_FAILURE_USER_DENY = 1;
    private static final int AUTH_FAILURE_ERROR_RECEIVED = 2;
    private static final int AUTH_FAILURE_TIMEOUT = 3;

    private final Handler mHandler = new Handler();

    private WebView mWebView;
    private OAuthService mService;
    private Token mRequestToken;
    private ProgressBar mProgressBar;
    private String mAccessToken;
    private boolean mTimeout;


    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress == 100) {
                mProgressBar.setVisibility(View.GONE);
            } else {
                if (!mProgressBar.isShown()) {
                    mProgressBar.setVisibility(View.VISIBLE);
                }
                mProgressBar.setProgress(newProgress);
            }
            super.onProgressChanged(view, newProgress);
        }
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            L.d(TAG, url + " ---- loading...");
            if (url.contains("&oauth_verifier=")) {
                String verifier = (url.split("&oauth_verifier="))[1];
                if (verifier != null && verifier.length() == Constants.VERIFIER_LENGTH) {
                    L.d(TAG, "got verifier, next step...");
                    new GetTrelloAccessTokenTask().execute(verifier);
                } else {
                    L.d(TAG, "trello not response verifier");
                    finishFailure(AUTH_FAILURE_NO_VERIFIER);
                }
                return true;
            } else if (url.equals("https://trello.com/oob#token=")) {
                // user touch the Deny button
                L.d(TAG, "user deny auth");
                finishFailure(AUTH_FAILURE_USER_DENY);
                return true;
            } else {
                view.loadUrl(url);
                return true;
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            mTimeout = false;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            L.d(TAG, "onReceivedError --- " + "errorCode=" + errorCode);
            L.d(TAG, "onReceivedError --- " + "description=" + description);
            L.d(TAG, "onReceivedError --- " + "failingUrl=" + failingUrl);
            finishFailure(AUTH_FAILURE_ERROR_RECEIVED);
        }
    }

    private CookieManager mCookieManager;

    private UIHandler mUICallback = new UIHandler(this);

    static class UIHandler extends Handler {
        WeakReference<AuthenticatorActivity> mActivity;

        UIHandler(AuthenticatorActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            AuthenticatorActivity theActivity = mActivity.get();
            switch (msg.what) {
                case VelloService.MSG_RETURN_TRELLO_USERNAME:
                    String username = (String) msg.obj;
                    theActivity.finishAuthenticated(username);
                    break;
            }
        }
    }

    Messenger mVelloService = null;
    private boolean mIsBound;

    final Messenger mMessenger = new Messenger(mUICallback);

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mVelloService = new Messenger(service);

            try {
                Message msg = Message.obtain(null, VelloService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mVelloService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do
                // anything with it
            }

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mTimeout) {
                        finishFailure(AUTH_FAILURE_TIMEOUT);
                    }
                }
            }, AUTH_TIMEOUT_IN_MILLIS);

            new GetTrelloAuthVerifierStringTask().execute();

        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };

    private void sendMessageToService(int type, Object obj) {
        if (mIsBound) {
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, type);
                    if (obj != null) {
                        msg.obj = obj;
                    }
                    msg.replyTo = mMessenger;
                    mVelloService.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void doBindService() {
        bindService(new Intent(this, VelloService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            if (mVelloService != null) {
                try {
                    Message msg = Message.obtain(null, VelloService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mVelloService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }

            }
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth);
        mTimeout = true;

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.setWebChromeClient(new MyWebChromeClient());

        CookieSyncManager.createInstance(getApplicationContext());
        mCookieManager = CookieManager.getInstance();

        doBindService();
    }

    @Override
    protected void onDestroy() {
        mWebView.clearCache(true);
        doUnbindService();
        super.onDestroy();
    }

    private void finishAuthenticated(String username) {
        L.i(TAG, "finishAuthenticated(). token=" + mAccessToken + ", username=" + username);

        AccountUtils.addAccount(getApplicationContext(), username, Constants.ACCOUNT_TYPE);
        AccountUtils.setAuthToken(getApplicationContext(), username, Constants.ACCOUNT_TYPE, mAccessToken);

        final Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, username);
        setAccountAuthenticatorResult(intent.getExtras());

        // remove account related old info.
        mCookieManager.removeAllCookie();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sp.edit().clear().apply();

        Toast.makeText(C.get(), R.string.toast_auth_finished, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void finishFailure(int type) {
        Log.i(TAG, "finishFailure() --- " + "type=" + type);
        switch (type) {
            case AUTH_FAILURE_TIMEOUT:
                Toast.makeText(C.get(), R.string.msg_auth_timeout, Toast.LENGTH_LONG).show();
                break;
            case AUTH_FAILURE_USER_DENY:
            case AUTH_FAILURE_NO_VERIFIER:
            case AUTH_FAILURE_ERROR_RECEIVED:
            default:
                Toast.makeText(C.get(), R.string.msg_auth_failure, Toast.LENGTH_LONG).show();
                break;
        }
        final Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_ERROR_CODE, AccountManager.ERROR_CODE_REMOTE_EXCEPTION);
        setAccountAuthenticatorResult(intent.getExtras());
        mCookieManager.removeAllCookie();
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
            if (!mProgressBar.isShown()) {
                mProgressBar.setVisibility(View.VISIBLE);
            }
            L.d(TAG, "result="+result);
            mWebView.loadUrl(result);
        }

    }

    class GetTrelloAccessTokenTask extends AsyncTask<String, Void, Token> {

        @Override
        protected Token doInBackground(String... params) {
            Verifier verifier = new Verifier(params[0]);
            return mService.getAccessToken(mRequestToken, verifier);
        }

        @Override
        protected void onPostExecute(Token token) {
            mAccessToken = token.getToken();
            sendMessageToService(VelloService.MSG_READ_TRELLO_ACCOUNT_USERNAME, mAccessToken);
        }
    }
}
