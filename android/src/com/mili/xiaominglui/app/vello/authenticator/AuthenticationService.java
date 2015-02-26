package com.mili.xiaominglui.app.vello.authenticator;

import com.mili.xiaominglui.app.vello.base.log.L;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;



/**
 * Service to handle Account authentication. It instantiates the authenticator
 * and returns its IBinder.
 */
public class AuthenticationService extends Service {

    private static final String TAG = AuthenticationService.class.getSimpleName();

    private Authenticator mAuthenticator;

    @Override
    public void onCreate() {
        L.d(TAG, "AuthenticationService onCreate.");
        mAuthenticator = new Authenticator(this);
    }

    @Override
    public void onDestroy() {
        L.d(TAG, "AuthenticationService onDestroy.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        L.d(TAG, "AuthenticationService onBind.");
        return mAuthenticator.getIBinder();
    }
}
