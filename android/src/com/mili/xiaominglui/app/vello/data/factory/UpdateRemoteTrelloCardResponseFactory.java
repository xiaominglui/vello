package com.mili.xiaominglui.app.vello.data.factory;

import com.google.gson.Gson;
import com.mili.xiaominglui.app.vello.data.model.TrelloCard;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;

import android.os.Bundle;

public class UpdateRemoteTrelloCardResponseFactory {

	public static Bundle parseResult(String body) {
		Gson gson = new Gson();
		TrelloCard updatedWordCard = gson.fromJson(body, TrelloCard.class);
		Bundle bundle = new Bundle();
		if (updatedWordCard != null) {
			// update success
			bundle.putBoolean(VelloRequestFactory.BUNDLE_EXTRA_REMOTE_TRELLO_CARD_UPDATED, true);
		} else {
			// update failed
			bundle.putBoolean(VelloRequestFactory.BUNDLE_EXTRA_REMOTE_TRELLO_CARD_UPDATED, false);
		}
		return bundle;
	}
}
