package com.mili.xiaominglui.app.vello.data.factory;

import android.os.Bundle;

import com.google.gson.Gson;
import com.mili.xiaominglui.app.vello.data.model.List;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;

public class ReopenVocabularyListResponseJsonFactory {
    
    private ReopenVocabularyListResponseJsonFactory() {
	// No public constructor
    }

    public static Bundle parseResult(String wsResponse) {
	Gson gson = new Gson();
	List list = gson.fromJson(wsResponse, List.class);
	
	if (list.closed.equals("false")) {
	    Bundle bundle = new Bundle();
	    bundle.putString(VelloRequestFactory.BUNDLE_EXTRA_VOCABULARY_LIST_ID, list.id);
	    return bundle;
	}
	return null;
    }
}
