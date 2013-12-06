package com.mili.xiaominglui.app.vello.syncadapter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import com.foxykeep.datadroid.exception.ConnectionException;
import com.foxykeep.datadroid.exception.DataException;
import com.foxykeep.datadroid.network.NetworkConnection;
import com.foxykeep.datadroid.network.NetworkConnection.ConnectionResult;
import com.foxykeep.datadroid.network.NetworkConnection.Method;
import com.google.gson.Gson;
import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.config.VelloConfig;
import com.mili.xiaominglui.app.vello.config.WSConfig;
import com.mili.xiaominglui.app.vello.data.factory.DictCardsHandler;
import com.mili.xiaominglui.app.vello.data.factory.JSONHandler;
import com.mili.xiaominglui.app.vello.data.factory.TrelloCardListJsonFactory;
import com.mili.xiaominglui.app.vello.data.model.AuthTokenRevokedOrCardDeletedResponse;
import com.mili.xiaominglui.app.vello.data.model.TrelloCard;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbDictCard;
import com.mili.xiaominglui.app.vello.data.provider.VelloProvider;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbWordCard;
import com.mili.xiaominglui.app.vello.data.provider.util.ProviderCriteria;
import com.mili.xiaominglui.app.vello.service.VelloService;
import com.mili.xiaominglui.app.vello.ui.MainActivity;
import com.mili.xiaominglui.app.vello.util.AccountUtils;

import android.accounts.Account;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.sax.StartElementListener;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class SyncHelper {
	private static final String TAG = SyncHelper.class.getSimpleName();
	
	public static final int FLAG_SYNC_LOCAL = 0x1;
    public static final int FLAG_SYNC_REMOTE = 0x2;
    
    private static final int LOCAL_VERSION_CURRENT = 25;
	
	private Context mContext;
    private String mAuthToken;
	
	public SyncHelper(Context context) {
        mContext = context;
        mAuthToken = AccountUtils.getAuthToken(mContext);
    }
	
	public static void requestManualSync(Account mChosenAccount) {
        Bundle b = new Bundle();
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(
                mChosenAccount,
                VelloProvider.AUTHORITY, b);
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
			Log.i(TAG, "found localVersion=" + localVersion + " and LOCAL_VERSION_CURRENT="
                    + LOCAL_VERSION_CURRENT);
			// Only run local sync if there's a newer version of data available
            // than what was last locally-sync'd.
			if (localParse) {
				// Load static local data
				
				// Clear the table
				resolver.delete(DbDictCard.CONTENT_URI, null, null);
				
				try {
					ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
					Log.i(TAG, "Local syncing dictionary data");
					Log.d("mingo.lv", "syncing dictionary data start...");
					batch.addAll(new DictCardsHandler(mContext).parse(JSONHandler.parseResource(mContext, R.raw.word0)));
					resolver.applyBatch(VelloProvider.AUTHORITY, batch);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (OperationApplicationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Log.d("mingo.lv", "syncing dictionary data end.");
			}
			Log.i(TAG, "Local sync took " + (System.currentTimeMillis() - startLocal) + "ms");
		}
		
		if ((flags & FLAG_SYNC_REMOTE) != 0) {
			Intent intent = new Intent(mContext, VelloService.class);
			mContext.startService(intent);
			
			/*
			// query and backup all local items that syncInNext=true or merge
			// locally later
			HashMap<String, WordCard> localDirtyWords = new HashMap<String, WordCard>();
			ProviderCriteria criteria = new ProviderCriteria();
			criteria.addNe(DbWordCard.Columns.DATE_LAST_OPERATION, "");
			Cursor c = resolver.query(DbWordCard.CONTENT_URI,
					DbWordCard.PROJECTION, criteria.getWhereClause(),
					criteria.getWhereParams(), criteria.getOrderClause());
			if (c != null) {
				while (c.moveToNext()) {
					WordCard wc = new WordCard(c);
					localDirtyWords.put(wc.id, wc);
				}
			}

			try {
				ArrayList<WordCard> preSyncRemoteWordCardList = getOpenWordCards();
				if (preSyncRemoteWordCardList.size() > 0) {
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
					for (WordCard wordCard : preSyncRemoteWordCardList) {
						String idCard = wordCard.id;
						if (localDirtyWords.containsKey(idCard)) {
							// need merging
							WordCard dirtyWordCard = localDirtyWords.get(idCard);
							String stringLocalDateLastActivity = dirtyWordCard.dateLastActivity;
							String markDeleted = dirtyWordCard.markDeleted;
							String stringRemoteDateLastActivity = wordCard.dateLastActivity;
							
							if (markDeleted.equals("true")) {
								// delete word remotely
								deleteRemoteWordCard(dirtyWordCard);
							} else if (stringLocalDateLastActivity
									.equals(stringRemoteDateLastActivity)) {
								// remote has no commit
								// commit local due, closed, listId to remote
								updateRemoteWordCard(dirtyWordCard);
							} else {
								// remote has commit
								// update remote data based on local data
								WordCard newWordCard = upgradeWordCard(wordCard,
										dirtyWordCard);
								updateRemoteWordCard(newWordCard);
							}
						}
					}
				}

				// Clear the table
				resolver.delete(DbWordCard.CONTENT_URI, null, null);

				ArrayList<WordCard> postSyncRemoteWordCardList = getOpenWordCards();
				if (postSyncRemoteWordCardList.size() > 0) {
					ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
					for (WordCard wordCard : postSyncRemoteWordCardList) {
						operationList.add(ContentProviderOperation
								.newInsert(DbWordCard.CONTENT_URI)
								.withValues(wordCard.toContentValues()).build());
					}

					resolver.applyBatch(VelloProvider.AUTHORITY, operationList);

					// Build notification
					// TODO need rework
//					Intent intent = new Intent(mContext, MainActivity.class);
					PendingIntent pIntent = PendingIntent.getActivity(mContext, 0,
							intent, 0);

					ProviderCriteria cri = new ProviderCriteria();
					cri.addSortOrder(DbWordCard.Columns.DUE, true);

					Calendar rightNow = Calendar.getInstance();
					long rightNowUnixTime = rightNow.getTimeInMillis();
					long rightNowUnixTimeGMT = rightNowUnixTime - TimeZone.getDefault().getRawOffset();
					SimpleDateFormat fo = new SimpleDateFormat(
							"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
					String now = fo.format(new Date(rightNowUnixTimeGMT));
					cri.addLt(DbWordCard.Columns.DUE, now, true);
					cri.addNe(DbWordCard.Columns.CLOSED, "true");
					Cursor cur = mContext.getContentResolver().query(
							DbWordCard.CONTENT_URI, DbWordCard.PROJECTION,
							cri.getWhereClause(), cri.getWhereParams(),
							cri.getOrderClause());
					if (cur != null) {
						int num = cur.getCount();
						if (num > 0) {
							NotificationManager notificationManager = (NotificationManager) mContext
									.getSystemService(Context.NOTIFICATION_SERVICE);

							NotificationCompat.Builder builder = new NotificationCompat.Builder(
									mContext)
									.setWhen(rightNow.getTimeInMillis())
									.setSmallIcon(R.drawable.ic_stat_vaa)
									.setContentTitle(mContext.getText(R.string.notif_content_title))
									.setContentText(mContext.getText(R.string.notif_content_text))
									.setContentInfo(String.valueOf(num))
									.setContentIntent(pIntent)
									.setAutoCancel(true);

							notificationManager.notify(0, builder.build());
						}
					}
				}

			} catch (ConnectionException e) {
				// TODO if the token revoked at other place, ConnectionException will throw out 
				e.printStackTrace();
			} catch (DataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OperationApplicationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			*/
		}
		

		
	}
	
	private boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		return cm.getActiveNetworkInfo() != null
				&& cm.getActiveNetworkInfo().isConnectedOrConnecting();
	}
}
