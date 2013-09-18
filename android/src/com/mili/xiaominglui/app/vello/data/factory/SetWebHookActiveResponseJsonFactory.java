package com.mili.xiaominglui.app.vello.data.factory;

import com.google.gson.Gson;
import com.mili.xiaominglui.app.vello.data.model.WebHook;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;

import android.os.Bundle;

public class SetWebHookActiveResponseJsonFactory {

	public static Bundle parseResult(String wsResponse) {
		Gson gson = new Gson();
		WebHook wh = gson.fromJson(wsResponse, WebHook.class);
		if (wh != null) {
			Bundle bundle = new Bundle();
			bundle.putBoolean(VelloRequestFactory.BUNDLE_EXTRA_WEBHOOK_ACTIVE, Boolean.valueOf(wh.active));
			return bundle;
		}
		return null;
	}

}
