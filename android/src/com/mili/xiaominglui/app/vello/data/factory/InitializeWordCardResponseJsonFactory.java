package com.mili.xiaominglui.app.vello.data.factory;

import android.os.Bundle;

import com.google.gson.Gson;
import com.mili.xiaominglui.app.vello.data.model.WordCard;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;

public class InitializeWordCardResponseJsonFactory {
    public static final String TAG = InitializeWordCardResponseJsonFactory.class.getSimpleName();
    

    public static Bundle parseResult(String wsResponse) {
	Gson gson = new Gson();
	WordCard wordcard = gson.fromJson(wsResponse, WordCard.class);
	if (wordcard != null) {
	    Bundle bundle = new Bundle();
	    bundle.putParcelable(VelloRequestFactory.BUNDLE_EXTRA_WORDCARD, wordcard);
	    return bundle;
	}
	return null;
    }

}
