package com.mili.xiaominglui.app.vello.data.factory;

import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;

import android.os.Bundle;

public class MiliDictionaryResponseJsonFactory {

	public static Bundle parseResult(String wsResponse) {
		Bundle bundle = new Bundle();
		if (wsResponse != null) {

			bundle.putString(VelloRequestFactory.BUNDLE_EXTRA_DICTIONARY_WS_RESPONSE, wsResponse);
		}
		return bundle;
	}
}
