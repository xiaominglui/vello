package com.mili.xiaominglui.app.vello.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.requestmanager.RequestManager.RequestListener;
import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.config.VelloConfig;
import com.mili.xiaominglui.app.vello.data.model.WordCard;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent;
import com.mili.xiaominglui.app.vello.data.provider.VelloProvider;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbWordCard;
import com.mili.xiaominglui.app.vello.data.provider.util.ProviderCriteria;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestManager;
import com.mili.xiaominglui.app.vello.util.AccountUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class VelloService extends Service implements RequestListener {
    private static final String TAG = VelloService.class.getSimpleName();
    private NotificationManager mNM;

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = R.string.local_service_started;

    /**
     * Class for clients to access. Because we know this service always runs in
     * the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public VelloService getService() {
            return VelloService.this;
        }
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    
    @Override
    public void onCreate() {
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mRequestManager = VelloRequestManager.from(this);
        mRequestList = new ArrayList<Request>();
        
        // Display a notification about us starting.  We put an icon in the status bar.
//        showNotification();
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(NOTIFICATION);

        // Tell the user we stopped.
        Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();
    }
    
 // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();
    
    protected VelloRequestManager mRequestManager;
    protected ArrayList<Request> mRequestList;
    
    private String[] mProjection = {
            VelloContent.DbWordCard.Columns.ID.getName(),
            VelloContent.DbWordCard.Columns.ID_CARD.getName(),
            VelloContent.DbWordCard.Columns.ID_LIST.getName()};
    
    private void syncTrelloDB() {
        if (VelloConfig.DEBUG_SWITCH) {
            Log.d(TAG, "syncTrelloDB start...");
        }
        Request syncTrelloDB = VelloRequestFactory.syncTrelloDBRequest();
        mRequestManager.execute(syncTrelloDB, this);
        mRequestList.add(syncTrelloDB);
    }
    
    private void archiveWordCard(String idCard) {
        // TODO
    }
    
    private void reviewedWordCard(String idCard, int position) {
        // TODO
//      mRefreshActionItem.showProgress(true);
        if (VelloConfig.DEBUG_SWITCH) {
            Log.d(TAG, "reviewedWordCard start...");
        }
        Request reviewedWordCard = VelloRequestFactory.reviewedWordCardRequest(
                idCard, position);
        mRequestManager.execute(reviewedWordCard, this);
        mRequestList.add(reviewedWordCard);
    }

    @Override
    public void onRequestFinished(Request request, Bundle resultData) {
        if (mRequestList.contains(request)) {
            mRequestList.remove(request);
            
            switch (request.getRequestType()) {
            case VelloRequestFactory.REQUEST_TYPE_SYNC_TRELLODB:
                ArrayList<WordCard> remoteWordCardList = resultData.getParcelableArrayList(VelloRequestFactory.BUNDLE_EXTRA_WORDCARD_LIST);
                int wordCardListSize = remoteWordCardList.size();
                if (wordCardListSize > 0) {
                    ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
                    Calendar rightNow = Calendar.getInstance();
                    long rightNowUnixTime = rightNow.getTimeInMillis();
                    SimpleDateFormat format = new SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    Date date;

                    for (WordCard wordCard : remoteWordCardList) {
                        String dueString = wordCard.due;
                        String id = wordCard.idCard;
                        if (!dueString.equals("null")) {
                            try {
                                date = format.parse(dueString);
                                long dueUnixTime = date.getTime();
                                if (dueUnixTime <= rightNowUnixTime) {
                                    // it is time to review, insert words to local DB
                                    // cache
                                    ProviderCriteria criteria = new ProviderCriteria(VelloContent.DbWordCard.Columns.ID_CARD, id);
                                    Cursor c = getContentResolver().query(VelloContent.DbWordCard.CONTENT_URI, mProjection, criteria.getWhereClause(), criteria.getWhereParams(), criteria.getOrderClause());
                                    if (c != null) {
                                        // if local DB cache has the word && syncInNext = true
                                        // +1 and check to archive or update word card
                                        while (c.moveToNext()) {
                                            String idCard = c.getString(DbWordCard.Columns.ID_CARD.getIndex());
                                            String syncInNext = c.getString(DbWordCard.Columns.SYNCINNEXT.getIndex());
                                            String idList = c.getString(DbWordCard.Columns.ID_LIST.getIndex());
                                            if (syncInNext.equals("true")) {
                                                int position = AccountUtils.getVocabularyListPosition(this, idList);
                                                if (position == VelloConfig.VOCABULARY_LIST_POSITION_8TH) {
                                                    // archive this word && delete word row
                                                    archiveWordCard(idCard);
                                                } else {
                                                    // TODO
                                                    // +1 and update wordcard
                                                    reviewedWordCard(idCard, position);
                                                    // +1 and update word row
                                                }
                                            }
                                        }
                                        
                                    } else {
                                        // local DB cache has NOT the word, insert directly
                                        operationList.add(ContentProviderOperation
                                                .newInsert(DbWordCard.CONTENT_URI)
                                                .withValues(wordCard.toContentVaalues())
                                                .build());
                                    }
                                }
                            } catch (ParseException e) {
                                Log.e(TAG, "ParseException", e);
                            }
                        }
                    }
                    
                    try {
                        getContentResolver().applyBatch(VelloProvider.AUTHORITY, operationList);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    } catch (OperationApplicationException e) {
                        e.printStackTrace();
                    }
                }
                return;
            case VelloRequestFactory.REQUEST_TYPE_REVIEWED_WORDCARD:
                WordCard reviewedWordCard = resultData
                        .getParcelable(VelloRequestFactory.BUNDLE_EXTRA_WORDCARD);
                if (reviewedWordCard != null) {
                    // reviewed

                } else {
                    // reviewed failed
                    // do nothing at present
                }
                // TODO
//              showCurrentBadge();
                if (VelloConfig.DEBUG_SWITCH) {
                    Log.d(TAG, "reviewedWordCard end.");
                }
                return;
            default:
                return;
            }
        }
    }

    @Override
    public void onRequestConnectionError(Request request, int statusCode) {
        // TODO Auto-generated method stub
        if (mRequestList.contains(request)) {
//          setProgressBarIndeterminateVisibility(false);
            mRequestList.remove(request);

//          ConnectionErrorDialogFragment.show(this, request, this);
        }

    }

    @Override
    public void onRequestDataError(Request request) {
        // TODO
        if (mRequestList.contains(request)) {
//          mRefreshActionItem.showProgress(false);
            mRequestList.remove(request);

//          showBadDataErrorDialog();
        }

    }

    @Override
    public void onRequestCustomError(Request request, Bundle resultData) {
        // Never called.

    }
}