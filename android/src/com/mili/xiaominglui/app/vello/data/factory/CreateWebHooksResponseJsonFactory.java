package com.mili.xiaominglui.app.vello.data.factory;

import com.google.gson.Gson;
import com.mili.xiaominglui.app.vello.data.model.WebHook;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;

import android.os.Bundle;

public class CreateWebHooksResponseJsonFactory {

	public static Bundle parseResult(String wsResponse) {
		Gson gson = new Gson();
		WebHook wh = gson.fromJson(wsResponse, WebHook.class);
		
		if (wh != null) {
			Bundle bundle = new Bundle();
			bundle.putString(VelloRequestFactory.BUNDLE_EXTRA_WEB_HOOK_ID, wh.id);
			return bundle;
		}
		return null;
	}
}
