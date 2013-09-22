package com.mili.xiaominglui.app.vello.data.factory;

import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;

import android.os.Bundle;

public class CheckTrelloConnectionResponseFactory {

	public static Bundle parseResult(String body) {
		Bundle bundle = new Bundle();
		bundle.putBoolean(VelloRequestFactory.BUNDLE_EXTRA_TRELLO_CONNECTION, true);
		return bundle;
	}
}
