package com.mili.xiaominglui.app.vello.data.factory;

import android.os.Bundle;

import com.google.gson.Gson;
import com.mili.xiaominglui.app.vello.data.model.VocabularyBoard;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;

public class ConfigureBoardResponseJsonFactory {
    
    private ConfigureBoardResponseJsonFactory() {
	// No public constructor
    }

    public static Bundle parseResult(String wsResponse) {
	Gson gson = new Gson();
	VocabularyBoard vb = gson.fromJson(wsResponse, VocabularyBoard.class);
	
	if (vb.closed.equals("true")) {
	    Bundle bundle = new Bundle();
	    bundle.putString(VelloRequestFactory.BUNDLE_EXTRA_VOCABULARY_BOARD_ID, vb.id);
	    return bundle;
	}
	return null;
    }
}
