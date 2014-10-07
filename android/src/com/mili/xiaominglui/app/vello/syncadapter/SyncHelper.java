package com.mili.xiaominglui.app.vello.syncadapter;

import java.io.IOException;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.mili.xiaominglui.app.vello.config.VelloConfig;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbDictCard;
import com.mili.xiaominglui.app.vello.data.provider.VelloProvider;
import com.mili.xiaominglui.app.vello.service.VelloService;

public class SyncHelper {
    private static final String TAG = SyncHelper.class.getSimpleName();

    public static final int FLAG_SYNC_LOCAL = 0x1;
    public static final int FLAG_SYNC_REMOTE = 0x2;

    private static final int LOCAL_VERSION_CURRENT = 25;

    private Context mContext;

    public SyncHelper(Context context) {
        mContext = context;
    }

    public static void requestManualSync(Account mChosenAccount) {
        Bundle b = new Bundle();
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(mChosenAccount, VelloProvider.AUTHORITY, b);
    }

    public void performSync(SyncResult syncResult, int flags) throws IOException {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        final int localVersion = prefs.getInt("local_data_version", 0);

        // Bulk of sync work, performed by executing several fetches from
        // local and online sources.
        final ContentResolver resolver = mContext.getContentResolver();


        if (!isOnline()) {
            return;
        }
        if (VelloConfig.DEBUG_SWITCH) {
            Log.d(TAG, "onPerformSync...");
        }

        if ((flags & FLAG_SYNC_LOCAL) != 0) {
            final long startLocal = System.currentTimeMillis();
            final boolean localParse = localVersion < LOCAL_VERSION_CURRENT;
            Log.i(TAG, "found localVersion=" + localVersion + " and LOCAL_VERSION_CURRENT=" + LOCAL_VERSION_CURRENT);
            // Only run local sync if there's a newer version of data available
            // than what was last locally-sync'd.
            if (localParse) {
                // Load static local data

                // Clear the table
                resolver.delete(DbDictCard.CONTENT_URI, null, null);
            }
            Log.i(TAG, "Local sync took " + (System.currentTimeMillis() - startLocal) + "ms");
        }

        if ((flags & FLAG_SYNC_REMOTE) != 0) {
            Intent intent = new Intent(mContext, VelloService.class);
            intent.putExtra("sync", true);
            mContext.startService(intent);
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}
