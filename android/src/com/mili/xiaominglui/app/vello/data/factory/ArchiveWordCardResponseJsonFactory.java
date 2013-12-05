package com.mili.xiaominglui.app.vello.data.factory;

import com.google.gson.Gson;
import com.mili.xiaominglui.app.vello.data.model.TrelloCard;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;

import android.os.Bundle;

public class ArchiveWordCardResponseJsonFactory {

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
