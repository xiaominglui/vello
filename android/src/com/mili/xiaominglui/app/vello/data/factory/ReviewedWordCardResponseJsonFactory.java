package com.mili.xiaominglui.app.vello.data.factory;

import android.os.Bundle;

import com.google.gson.Gson;
import com.mili.xiaominglui.app.vello.data.model.TrelloCard;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;

public class ReviewedWordCardResponseJsonFactory {
    private static final String TAG = ReviewedWordCardResponseJsonFactory.class.getSimpleName();

	public static Bundle parseResult(String wsResponse) {
		Gson gson = new Gson();
		TrelloCard wordcard = gson.fromJson(wsResponse, TrelloCard.class);
		if (wordcard != null) {
			Bundle bundle = new Bundle();
			bundle.putParcelable(VelloRequestFactory.BUNDLE_EXTRA_WORDCARD,
					wordcard);
			return bundle;
		}
		return null;
	}
}
