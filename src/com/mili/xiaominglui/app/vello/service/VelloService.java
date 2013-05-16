package com.mili.xiaominglui.app.vello.service;

import java.util.ArrayList;

import com.foxykeep.datadroid.requestmanager.Request;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestManager;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class VelloService extends Service {
	
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
		
	}

}
