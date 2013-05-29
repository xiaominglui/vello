package com.mili.xiaominglui.app.vello.service;

import java.util.ArrayList;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.requestmanager.RequestManager.RequestListener;
import com.mili.xiaominglui.app.vello.config.VelloConfig;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestManager;

public class VelloService extends IntentService implements RequestListener {
	private static final String TAG = VelloService.class.getSimpleName();
	
	protected VelloRequestManager mRequestManager;
	protected ArrayList<Request> mRequestList;
	
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

    @Override
    public void onRequestFinished(Request request, Bundle resultData) {
    	if (mRequestList.contains(request)) {
    		mRequestList.remove(request);
    		
    		switch (request.getRequestType()) {
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
