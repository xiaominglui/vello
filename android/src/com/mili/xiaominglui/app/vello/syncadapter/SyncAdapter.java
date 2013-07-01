package com.mili.xiaominglui.app.vello.syncadapter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.ServiceConnection;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.foxykeep.datadroid.exception.ConnectionException;
import com.foxykeep.datadroid.exception.DataException;
import com.foxykeep.datadroid.network.NetworkConnection;
import com.foxykeep.datadroid.network.NetworkConnection.ConnectionResult;
import com.foxykeep.datadroid.network.NetworkConnection.Method;
import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.config.VelloConfig;
import com.mili.xiaominglui.app.vello.config.WSConfig;
import com.mili.xiaominglui.app.vello.data.factory.AllWordCardListJsonFactory;
import com.mili.xiaominglui.app.vello.data.model.WordCard;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbWordCard;
import com.mili.xiaominglui.app.vello.data.provider.VelloProvider;
import com.mili.xiaominglui.app.vello.data.provider.util.ProviderCriteria;
import com.mili.xiaominglui.app.vello.service.VelloService;
import com.mili.xiaominglui.app.vello.ui.MainActivity;
import com.mili.xiaominglui.app.vello.util.AccountUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;


public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = SyncAdapter.class.getSimpleName();
    private static final String SYNC_MARKER_KEY = "com.example.android.samplesync.marker";
    private static final boolean NOTIFY_AUTH_FAILURE = true;

    private final AccountManager mAccountManager;

    private final Context mContext;
    
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
            // Tell the user about this for our demo.
            Toast.makeText(mContext, R.string.local_service_connected,
                    Toast.LENGTH_SHORT).show();

            try {
                Message msg = Message.obtain(null, VelloService.MSG_REGISTER_CLIENT);
//                msg.replyTo = mMessenger;
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
            Toast.makeText(mContext, R.string.local_service_disconnected,
                    Toast.LENGTH_SHORT).show();
        }
    };

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
    	
    	// bind VelloService
    	doBindService();
    	
    	// query and backup all local items that syncInNext=true or merge locally later
    	HashMap<String, WordCard> localDirtyWords = new HashMap<String, WordCard>();
    	final ContentResolver resolver = mContext.getContentResolver();
    	ProviderCriteria criteria = new ProviderCriteria();
    	criteria.addEq(DbWordCard.Columns.SYNCINNEXT, "true");
    	Cursor c = resolver.query(DbWordCard.CONTENT_URI, DbWordCard.PROJECTION, criteria.getWhereClause(), criteria.getWhereParams(), criteria.getOrderClause());
    	if (c != null) {
    	    while (c.moveToNext()) {
    	    	WordCard wc = new WordCard(c);
    	    	localDirtyWords.put(wc.idCard, wc);
    	    }
    	}
    	
    	// full sync due items and merge local dirty data
    	String token = AccountUtils.getAuthToken(mContext);
		String vocabularyBoardId = AccountUtils.getVocabularyBoardId(mContext);
		String urlString = WSConfig.TRELLO_API_URL
				+ WSConfig.WS_TRELLO_TARGET_BOARD + "/" + vocabularyBoardId
				+ WSConfig.WS_TRELLO_FIELD_CARDS;
		
		HashMap<String, String> parameterMap = new HashMap<String, String>();
		parameterMap.put(WSConfig.WS_TRELLO_PARAM_FILTER, "open");
		parameterMap.put(WSConfig.WS_TRELLO_PARAM_FIELDS,
				"name,desc,due,closed,idList,dateLastActivity");
		parameterMap.put(WSConfig.WS_TRELLO_PARAM_APP_KEY,
				WSConfig.VELLO_APP_KEY);
		parameterMap.put(WSConfig.WS_TRELLO_PARAM_ACCESS_TOKEN, token);
		
		NetworkConnection networkConnection = new NetworkConnection(mContext,
				urlString);
		networkConnection.setMethod(Method.GET);
		networkConnection.setParameters(parameterMap);
		ConnectionResult result;
		try {
			result = networkConnection.execute();
			ArrayList<WordCard> wordCardList = new ArrayList<WordCard>();
			wordCardList = AllWordCardListJsonFactory.parseResult(result.body);
			if (VelloConfig.DEBUG_SWITCH) {
				Log.d(TAG, "result.body = " + result.body);
			}
			
			// Clear the table
			mContext.getContentResolver().delete(DbWordCard.CONTENT_URI, null, null);
			
			int wordCardListSize = wordCardList.size();
			if (wordCardListSize > 0) {
				ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
				SimpleDateFormat format = new SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
				for (WordCard wordCard : wordCardList) {
					// merge
					String idCard = wordCard.idCard;
					
					if (localDirtyWords.containsKey(idCard)) {
						// need merging
						WordCard dirtyWordCard = localDirtyWords.get(idCard);
						String stringLocalDateLastActivity = dirtyWordCard.dateLastActivity;
						Date localDateLastActivity = new Date(stringLocalDateLastActivity);
						String stringRemoteDateLastActivity = wordCard.dateLastActivity;
						Date remoteDateLastActivity = new Date(stringRemoteDateLastActivity);
						int compare = remoteDateLastActivity.compareTo(localDateLastActivity);
						if (compare == 0) {
							// remote has no commit
							// commit local due, closed, listId to remote
							sendUpgradeRemoteWordCardMessage(dirtyWordCard);
							if (!dirtyWordCard.closed.equals("true")) {
								operationList.add(ContentProviderOperation
										.newInsert(DbWordCard.CONTENT_URI)
										.withValues(dirtyWordCard.toContentVaalues()).build());
							}
							
						} else if (compare > 0) {
							// remote has commit
							// update remote data based on local data
							WordCard newWordCard = upgradeWordCard(wordCard, dirtyWordCard);
							sendUpgradeRemoteWordCardMessage(newWordCard);
							operationList.add(ContentProviderOperation
									.newInsert(DbWordCard.CONTENT_URI)
									.withValues(newWordCard.toContentVaalues()).build());
						} else if (compare < 0) {
							// it should NOT happen
						}
						/*
						String localDue = localDirtyWords.get(idCard);
						if (!localDue.equals(wordCard.due)) {
							// reviewed + 1 remotelly
							sendReviewedPlusMessage(idCard);
						} else {
							sendReviewedMessage(idCard, positionList);
						}
						// listId + 1 locally && insert DB
						String oldListId = wordCard.idList;
						int oldPositionList = AccountUtils.getVocabularyListPosition(mContext, oldListId);
						if (oldPositionList != VelloConfig.VOCABULARY_LIST_POSITION_8TH) {
							wordCard.idList = AccountUtils.getVocabularyListId(mContext, oldPositionList + 1);
							operationList.add(ContentProviderOperation
									.newInsert(DbWordCard.CONTENT_URI)
									.withValues(wordCard.toContentVaalues()).build());
						}
						*/
					} else {
						// fast-forword insert
						operationList.add(ContentProviderOperation
								.newInsert(DbWordCard.CONTENT_URI)
								.withValues(wordCard.toContentVaalues()).build());
					}
				}

				try {
					mContext.getContentResolver().applyBatch(
							VelloProvider.AUTHORITY, operationList);
				} catch (RemoteException e) {
					throw new DataException(e);
				} catch (OperationApplicationException e) {
					throw new DataException(e);
				}

                Intent intent = new Intent(mContext, MainActivity.class);
                PendingIntent pIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

				// Build notification
				// TODO need rework
                ProviderCriteria cri = new ProviderCriteria();
                criteria.addSortOrder(DbWordCard.Columns.DUE, true);

                Calendar rightNow = Calendar.getInstance();
                SimpleDateFormat fo = new SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                String now = fo.format(rightNow.getTime());
                cri.addLt(DbWordCard.Columns.DUE, now, true);
                cri.addNe(DbWordCard.Columns.CLOSED, "true");
                Cursor cur = resolver.query(DbWordCard.CONTENT_URI, DbWordCard.PROJECTION, cri.getWhereClause(), cri.getWhereParams(), cri.getOrderClause());
                if (cur != null) {
                	int num = cur.getCount();
                	if (num > 0) {
						Notification noti = new Notification.Builder(mContext)
								.setContentTitle(
										"You have " + num + " words need reviewing!")
								.setContentText("Click me to begin reviewing!")
								.setSmallIcon(R.drawable.ic_launcher)
								.setContentIntent(pIntent).build();

						NotificationManager notificationManager = (NotificationManager) mContext
								.getSystemService(Context.NOTIFICATION_SERVICE);

						// Hide the notification after its selected
						noti.flags |= Notification.FLAG_AUTO_CANCEL;

						notificationManager.notify(0, noti);
                	}
                	
                }
			}
		} catch (ConnectionException e) {
			e.printStackTrace();
		} catch (DataException e) {
			e.printStackTrace();
		}

		
		doUnbindService();
    }

	private WordCard upgradeWordCard(WordCard wordCard, WordCard dirtyWordCard) {
		// TODO need a upgrade method
		return wordCard;
	}

    /**
     * upgrade trello card with newWordCard
     * @param newWordCard at present only due, listId, closed be commit to remote at present
     */
	private void sendUpgradeRemoteWordCardMessage(WordCard newWordCard) {
		// TODO Auto-generated method stub
		try {
			Message msg = Message.obtain(null, VelloService.MSG_UPGRADE_WORDCARD);
			msg.obj = newWordCard;
			mService.send(msg);
		} catch (RemoteException e) {
			
		}
		
	}

	private void sendReviewedMessage(String idCard, int positionList) {
    	try {
            Message msg = Message.obtain(null, VelloService.MSG_REVIEWED_WORDCARD);
            msg.arg1 = positionList;
            msg.obj = idCard;
            mService.send(msg);
        } catch (RemoteException e) {
        }
	}

	private void sendReviewedPlusMessage(String idCard) {
		try {
            Message msg = Message.obtain(null, VelloService.MSG_REVIEWED_PLUS_WORDCARD);
            msg.obj = idCard;
            mService.send(msg);
        } catch (RemoteException e) {
        }
	}

	private void sendCloseWordCardMsg(String idCard) {
		try {
            Message msg = Message.obtain(null, VelloService.MSG_CLOSE_WORDCARD);
            msg.obj = idCard;
            mService.send(msg);
        } catch (RemoteException e) {
        }
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
    
    void doBindService() {
        // Establish a connection with the service. We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
    	mContext.bindService(new Intent(mContext,
                VelloService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }
    
    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            mContext.unbindService(mConnection);
            mIsBound = false;
        }
    }
}