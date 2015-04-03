package com.mili.xiaominglui.app.vello.syncadapter;

import java.io.IOException;
import java.util.regex.Pattern;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.mili.xiaominglui.app.vello.base.C;
import com.mili.xiaominglui.app.vello.base.log.L;
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

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean wifiOnly = sharedPref.getBoolean(SettingsActivity.KEY_PREF_SYNC_WIFI_ONLY, false);

        if (VelloConfig.DEBUG_SWITCH) {
            Log.d(TAG, "onPerformSync --- " + "wifiOnly=" + wifiOnly);
        }

        WifiManager.WifiLock wifiLock = null;
        WakeLock wakeLock = null;

        try {
            boolean wifiNetwork = false;

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
                AccountUtils.signOut(C.get());
            } else {
                if (wifiOnly) {
                    WifiManager wifiManager = (WifiManager) this.getContext().getSystemService(Context.WIFI_SERVICE);
                    wifiLock = wifiManager.createWifiLock(TAG);
                    wifiLock.acquire();
                }

                PowerManager pm = (PowerManager) this.getContext().getSystemService(Context.POWER_SERVICE);
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
                wakeLock.acquire();


                // handle sync task below
                if (mSyncHelper == null) {
                    mSyncHelper = new SyncHelper(mContext);
                }
                C.removePreference(VelloService.KEY_SYNC_TIMESTAMP_END);
                C.removePreference(VelloService.KEY_SYNC_TIMESTAMP_BEGIN);
                mSyncHelper.performSync(syncResult, SyncHelper.FLAG_SYNC_REMOTE);

                for (; ; ) {
                    if (isFinished()) {
                        L.d(TAG, "sync finished()");
                        ++syncResult.stats.numUpdates;
                        return;
                    }

                    if (isTimeout()) {
                        L.d(TAG, "sync timeout");
                        ++syncResult.stats.numIoExceptions;
                        return;
                    }
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

    private boolean isTimeout() {
        long current = System.currentTimeMillis();
        long begin = C.getPreference(VelloService.KEY_SYNC_TIMESTAMP_BEGIN, -1L);
        if (begin > 0) {
            long diff = current - begin;
            if (diff > 60000) {
                return true;
            }
        }
        return false;
    }

    private boolean isFinished() {
        long syncEndTime = C.getPreference(VelloService.KEY_SYNC_TIMESTAMP_END, -1L);
        return syncEndTime > 0;
    }
}
