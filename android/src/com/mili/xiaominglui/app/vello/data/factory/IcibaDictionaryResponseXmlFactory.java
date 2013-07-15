package com.mili.xiaominglui.app.vello.data.factory;

import android.os.Bundle;

import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;

public class IcibaDictionaryResponseXmlFactory {

	public static Bundle parseResult(String wsResponse) {
		Bundle bundle = new Bundle();
		if (wsResponse != null) {

			bundle.putString(
					VelloRequestFactory.BUNDLE_EXTRA_DICTIONARY_WS_RESPONSE,
					wsResponse);
		}
		return bundle;
	}
}
