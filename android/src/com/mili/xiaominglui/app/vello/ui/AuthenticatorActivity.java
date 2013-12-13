
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.authenticator.Constants;
import com.mili.xiaominglui.app.vello.authenticator.TrelloAuthApi;
import com.mili.xiaominglui.app.vello.config.VelloConfig;
import com.mili.xiaominglui.app.vello.service.VelloService;

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
    private String mAccessToken;


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
	
	private UIHandler mUICallback = new UIHandler(this);
	private class UIHandler extends Handler {
		WeakReference<AuthenticatorActivity> mActivity;
		
		UIHandler(AuthenticatorActivity activity) {
			mActivity = new WeakReference<AuthenticatorActivity>(activity);
		}
		
		@Override
		public void handleMessage(Message msg) {
			AuthenticatorActivity theActivity = mActivity.get();
			switch (msg.what) {
			case VelloService.MSG_RETURN_TRELLO_USERNAME:
				String username = (String) msg.obj;
				finishAuthenticated(mAccessToken, username);
				break;
			}
		}
	}
	Messenger mVelloService = null;
	private boolean mIsBound;

	final Messenger mMessenger = new Messenger(mUICallback);

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service. Because we have bound to a explicit
			// service that we know is running in our own process, we can
			// cast its IBinder to a concrete class and directly access it.
			mVelloService = new Messenger(service);

			try {
				Message msg = Message.obtain(null,
						VelloService.MSG_REGISTER_CLIENT);
				msg.replyTo = mMessenger;
				mVelloService.send(msg);
			} catch (RemoteException e) {
				// In this case the service has crashed before we could even do
				// anything with it
			}
			
			new GetTrelloAuthVerifierStringTask().execute();
			
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected - process crashed.
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
				}
			}
		}
	}
	
	void doBindService() {
		// Establish a connection with the service. We use an explicit
		// class name because we want a specific service implementation that
		// we know will be running in our own process (and thus won't be
		// supporting component replacement by other applications).
		bindService(new Intent(this, VelloService.class), mConnection,
				Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	void doUnbindService() {
		if (mIsBound) {
			// Detach our existing connection.
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
    	
    	mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.setWebViewClient(new MyWebViewClient());
        
        doBindService();
    }
    
    @Override
    protected void onDestroy() {
    	mWebView.clearCache(true);
    	doUnbindService();
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
    private void finishAuthenticated(String token, String username) {
    	if (VelloConfig.DEBUG_SWITCH) {
    		Log.i(TAG, "finishAuthenticated(). token=" + token + ", username=" + username);
    	}

        final Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, username);
        intent.putExtra(AccountManager.KEY_PASSWORD, token);
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
			mWebView.setVisibility(View.INVISIBLE);
			
			mAccessToken = token.getToken();
			sendMessageToService(VelloService.MSG_READ_TRELLO_ACCOUNT_INFO, mAccessToken);
		}
    }
}
