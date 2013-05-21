package com.mili.xiaominglui.app.vello.service;

import java.util.ArrayList;

import com.foxykeep.datadroid.requestmanager.Request;
import com.mili.xiaominglui.app.vello.config.VelloConfig;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestManager;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class VelloService extends Service {
	private static final String TAG = VelloService.class.getSimpleName();
	
	protected VelloRequestManager mRequestManager;
	protected ArrayList<Request> mRequestList;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mRequestManager = VelloRequestManager.from(this);
		mRequestList = new ArrayList<Request>();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void syncTrelloDB() {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "syncTrelloDB start...");
		}
	}
}
