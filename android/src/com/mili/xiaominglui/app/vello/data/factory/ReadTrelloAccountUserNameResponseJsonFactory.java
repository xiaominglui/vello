package com.mili.xiaominglui.app.vello.data.factory;

import com.google.gson.Gson;
import com.mili.xiaominglui.app.vello.data.model.ReadTrelloAccountUserNameResponse;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;

import android.os.Bundle;

public class ReadTrelloAccountUserNameResponseJsonFactory {

	public static Bundle parseResult(String wsResponse) {
		Gson gson = new Gson();
		ReadTrelloAccountUserNameResponse response = gson.fromJson(wsResponse, ReadTrelloAccountUserNameResponse.class);
		Bundle bundle = new Bundle();
		if (response != null) {
			bundle.putString(VelloRequestFactory.BUNDLE_EXTRA_TRELLO_ACCOUNT_USERNAME, response._value);
		}
		return bundle;
	}
}
