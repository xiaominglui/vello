package com.mili.xiaominglui.app.vello.data.factory;

import com.google.gson.Gson;
import com.mili.xiaominglui.app.vello.data.model.WordCard;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;

import android.os.Bundle;

public class UpgradeWordCardResponseJsonFactory {
	private static final String TAG = UpgradeWordCardResponseJsonFactory.class.getSimpleName();

	public static Bundle parseResult(String wsResponse) {
		Gson gson = new Gson();
		WordCard wordcard = gson.fromJson(wsResponse, WordCard.class);
		if (wordcard != null) {
			Bundle bundle = new Bundle();
			bundle.putParcelable(VelloRequestFactory.BUNDLE_EXTRA_WORDCARD,
					wordcard);
			return bundle;
		}
		return null;
	}

}
