package com.mili.xiaominglui.app.vello.data.factory;

import android.os.Bundle;

import com.google.gson.Gson;
import com.mili.xiaominglui.app.vello.data.model.WordList;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;

public class ReviewedPlusWordCardResponseJsonFactory {

    public static Bundle parseResult(String wsResponse) {
        Gson gson = new Gson();
        WordList wordlist = gson.fromJson(wsResponse, WordList.class);
        if (wordlist != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(VelloRequestFactory.BUNDLE_EXTRA_WORDLIST, wordlist);
            return bundle;
        }
        return null;
    }

}
