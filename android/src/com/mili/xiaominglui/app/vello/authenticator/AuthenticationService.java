package com.mili.xiaominglui.app.vello.authenticator;

import com.mili.xiaominglui.app.vello.config.VelloConfig;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;



/**
 * Service to handle Account authentication. It instantiates the authenticator
 * and returns its IBinder.
 */
public class AuthenticationService extends Service {

    private static final String TAG = AuthenticationService.class.getSimpleName();

    private Authenticator mAuthenticator;

    @Override
    public void onCreate() {
    	if (VelloConfig.DEBUG_SWITCH) {
    		Log.d(TAG, "AuthenticationService onCreate.");
    	}

        mAuthenticator = new Authenticator(this);
    }

    @Override
    public void onDestroy() {
    	if (VelloConfig.DEBUG_SWITCH) {
    		Log.d(TAG, "AuthenticationService onDestroy.");
    	}
    }

    @Override
    public IBinder onBind(Intent intent) {
    	if (VelloConfig.DEBUG_SWITCH) {
    		Log.d(TAG, "AuthenticationService onBind.");
    	}
        return mAuthenticator.getIBinder();
    }
}
