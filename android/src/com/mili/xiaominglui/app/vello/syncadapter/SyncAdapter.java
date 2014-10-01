package com.mili.xiaominglui.app.vello.syncadapter;

import java.io.IOException;
import java.util.regex.Pattern;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.mili.xiaominglui.app.vello.config.VelloConfig;
import com.mili.xiaominglui.app.vello.service.VelloService;
import com.mili.xiaominglui.app.vello.ui.SettingsActivity;
import com.mili.xiaominglui.app.vello.util.AccountUtils;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = SyncAdapter.class.getSimpleName();
    private static final Pattern sSanitizeAccountNamePattern = Pattern.compile("(.).*?(.?)@");

    private final Context mContext;
    private ConnectivityManager mConnManager = null;
    private SyncHelper mSyncHelper;

    Messenger mService = null;
    private boolean mIsBound;
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service. Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mService = new Messenger(service);

            try {
                Message msg = Message.obtain(null, VelloService.MSG_REGISTER_CLIENT);
                mService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do
                // anything with it
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected - process crashed.
            mService = null;
        }
    };

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;

    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean wifiOnly = sharedPref.getBoolean(SettingsActivity.KEY_PREF_SYNC_WIFI_ONLY, false);

        if (VelloConfig.DEBUG_SWITCH) {
            Log.d(TAG, "onPerformSync --- " + "wifiOnly=" + wifiOnly);
        }

        WifiManager.WifiLock wifiLock = null;
        WakeLock wakeLock = null;

        try {
            boolean wifiNetwork = false;

            boolean isScheduleSyncTrigger = extras.getBoolean(SyncConstants.SYNC_BUNDLE_KEY_SCHEDULE_SYNC_TRIGGER, false);
            if (isScheduleSyncTrigger && !isLocalDataChanged(provider)) {
                if (VelloConfig.DEBUG_SWITCH) {
                    Log.v(TAG, "sync is trigged by schedule Sync and local has not changed, so refuse Continuing sync");
                }
                return;
            }


            if (mConnManager == null) {
                mConnManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            }
            NetworkInfo netInfo = mConnManager.getActiveNetworkInfo();
            if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                wifiNetwork = true;
            }

            if (wifiOnly && !wifiNetwork) {
                Log.i(TAG, authority + ": do not auto sync without WiFi!");
                syncResult.stats.numIoExceptions++;
                return;
            }

            final String logSanitizedAccountName = sSanitizeAccountNamePattern.matcher(account.name).replaceAll("$1...$2@");
            String chosenAccountName = AccountUtils.getAccountName(mContext);
            boolean isAccountSet = !TextUtils.isEmpty(chosenAccountName);
            boolean isChosenAccount = isAccountSet && chosenAccountName.equals(account.name);

            if (!isAccountSet || !isChosenAccount) {
                if (VelloConfig.DEBUG_SWITCH) {
                    Log.d(TAG, "Tried to sync account " + logSanitizedAccountName + " but the chosen " +
                            "account is actually " + chosenAccountName);
                }
                ++syncResult.stats.numAuthExceptions;
                return;
            }

            String token = AccountUtils.getAuthToken(mContext);
            if (token.equals("")) {
                syncResult.stats.numAuthExceptions++;
                // TODO auth failed now, what to do.
            } else {
                if (wifiOnly) {
                    WifiManager wifiManager =
                            (WifiManager) this.getContext().getSystemService(Context.WIFI_SERVICE);
                    wifiLock = wifiManager.createWifiLock(TAG);
                    wifiLock.acquire();
                }

                PowerManager pm = (PowerManager) this.getContext().getSystemService(Context.POWER_SERVICE);
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
                wakeLock.acquire();


                // handle sync task below
                if (true) {
                    if (mSyncHelper == null) {
                        mSyncHelper = new SyncHelper(mContext);
                    }
                    mSyncHelper.performSync(syncResult, SyncHelper.FLAG_SYNC_REMOTE);
                }
            }
        } catch (IOException e) {
            ++syncResult.stats.numIoExceptions;
        } finally {
            if (wakeLock != null) {
                wakeLock.release();
            }
            if (wifiLock != null) {
                wifiLock.release();
            }
        }
    }

    void doBindService() {
        // Establish a connection with the service. We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        mContext.bindService(new Intent(mContext, VelloService.class),
                mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            mContext.unbindService(mConnection);
            mIsBound = false;
        }
    }

    /**
     * check if local data has changed
     *
     * @param provider
     * @return true if changed, false not.
     */
    private boolean isLocalDataChanged(ContentProviderClient provider) {
        return false;
    }
}
