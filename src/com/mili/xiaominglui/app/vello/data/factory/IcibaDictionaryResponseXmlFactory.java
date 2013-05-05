package com.mili.xiaominglui.app.vello.data.factory;

import android.os.Bundle;

import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;

public class IcibaDictionaryResponseXmlFactory {

    public static Bundle parseResult(String wsResponse) {
	if (wsResponse != null) {
	    Bundle bundle = new Bundle();
	    bundle.putString(VelloRequestFactory.BUNDLE_EXTRA_DICTIONARY_ICIBA_RESPONSE, wsResponse);
	    return bundle;
	}
	return null;
    }
}
