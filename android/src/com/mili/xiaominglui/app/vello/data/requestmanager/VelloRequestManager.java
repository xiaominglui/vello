package com.mili.xiaominglui.app.vello.data.requestmanager;

import android.content.Context;

import com.foxykeep.datadroid.requestmanager.RequestManager;
import com.mili.xiaominglui.app.vello.data.service.VelloRequestService;

public class VelloRequestManager extends RequestManager {
    // Singleton management
    private static VelloRequestManager sInstance;
    
    public static VelloRequestManager from(Context context) {
	if (sInstance == null) {
	    sInstance = new VelloRequestManager(context);
	}
	
	return sInstance;
    }

    private VelloRequestManager(Context context) {
	super(context, VelloRequestService.class);
    }
}
