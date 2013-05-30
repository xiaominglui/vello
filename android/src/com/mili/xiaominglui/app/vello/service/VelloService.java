package com.mili.xiaominglui.app.vello.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.foxykeep.datadroid.exception.DataException;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.requestmanager.RequestManager.RequestListener;
import com.mili.xiaominglui.app.vello.config.VelloConfig;
import com.mili.xiaominglui.app.vello.data.model.WordCard;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent;
import com.mili.xiaominglui.app.vello.data.provider.VelloProvider;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbWordCard;
import com.mili.xiaominglui.app.vello.data.provider.util.ProviderCriteria;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestManager;
import com.mili.xiaominglui.app.vello.util.AccountUtils;

public class VelloService extends IntentService implements RequestListener {
	private static final String TAG = VelloService.class.getSimpleName();
	
	protected VelloRequestManager mRequestManager;
	protected ArrayList<Request> mRequestList;
	
	private String[] mProjection = {
			VelloContent.DbWordCard.Columns.ID.getName(),
			VelloContent.DbWordCard.Columns.ID_CARD.getName(),
			VelloContent.DbWordCard.Columns.ID_LIST.getName() };
	
	public VelloService() {
		super("VelloService");
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		mRequestManager = VelloRequestManager.from(this);
		mRequestList = new ArrayList<Request>();
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		syncTrelloDB();
	}

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
//		mRefreshActionItem.showProgress(true);
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
//				showCurrentBadge();
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
//			setProgressBarIndeterminateVisibility(false);
			mRequestList.remove(request);

//			ConnectionErrorDialogFragment.show(this, request, this);
		}

    }

    @Override
    public void onRequestDataError(Request request) {
    	// TODO
    	if (mRequestList.contains(request)) {
//			mRefreshActionItem.showProgress(false);
			mRequestList.remove(request);

//			showBadDataErrorDialog();
		}

    }

    @Override
    public void onRequestCustomError(Request request, Bundle resultData) {
    	// Never called.

    }
}
