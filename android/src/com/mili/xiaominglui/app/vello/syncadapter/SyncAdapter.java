package com.mili.xiaominglui.app.vello.syncadapter;

import com.mili.xiaominglui.app.vello.config.VelloConfig;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbWordCard;
import com.mili.xiaominglui.app.vello.data.provider.util.ProviderCriteria;
import com.mili.xiaominglui.app.vello.service.VelloService;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;


public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = SyncAdapter.class.getSimpleName();
    private static final String SYNC_MARKER_KEY = "com.example.android.samplesync.marker";
    private static final boolean NOTIFY_AUTH_FAILURE = true;

    private final AccountManager mAccountManager;

    private final Context mContext;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
        mAccountManager = AccountManager.get(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
        ContentProviderClient provider, SyncResult syncResult) {
    	if (VelloConfig.DEBUG_SWITCH) {
    		Log.d(TAG, "onPerformSync...");
    	}
    	// TODO
//    	Intent intent = new Intent(mContext, VelloService.class);
//    	mContext.startService(intent);
    	// query all local items that syncInNext=true
    	final String[] PROJECTION = new String[] {
    	        DbWordCard.Columns.ID_CARD.getName(),
    	        DbWordCard.Columns.ID_LIST.getName(),
    	        DbWordCard.Columns.DUE.getName(),
    	        DbWordCard.Columns.DATE_LAST_ACTIVITY.getName()
    	};
    	final ContentResolver resolver = mContext.getContentResolver();
    	ProviderCriteria criteria = new ProviderCriteria();
    	criteria.addEq(DbWordCard.Columns.SYNCINNEXT, "true");
    	Cursor c = resolver.query(DbWordCard.CONTENT_URI, PROJECTION, criteria.getWhereClause(), criteria.getWhereParams(), criteria.getOrderClause());
    	// sync all local items above to Trello
    	if (c != null) {
    	    while (c.moveToNext()) {
    	        String idCard = c.getString(DbWordCard.Columns.ID_CARD.getIndex());
    	        String idList = c.getString(DbWordCard.Columns.ID_LIST.getIndex());
    	    }
    	}
    	
    	// full sync due items from trello
    }

    /**
     * This helper function fetches the last known high-water-mark
     * we received from the server - or 0 if we've never synced.
     * @param account the account we're syncing
     * @return the change high-water-mark
     */
    private long getServerSyncMarker(Account account) {
        String markerString = mAccountManager.getUserData(account, SYNC_MARKER_KEY);
        if (!TextUtils.isEmpty(markerString)) {
            return Long.parseLong(markerString);
        }
        return 0;
    }

    /**
     * Save off the high-water-mark we receive back from the server.
     * @param account The account we're syncing
     * @param marker The high-water-mark we want to save.
     */
    private void setServerSyncMarker(Account account, long marker) {
        mAccountManager.setUserData(account, SYNC_MARKER_KEY, Long.toString(marker));
    }
}