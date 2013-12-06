package com.mili.xiaominglui.app.vello.data.factory;

import android.os.Bundle;

import com.google.gson.Gson;
import com.mili.xiaominglui.app.vello.data.model.AuthTokenRevokedOrCardDeletedResponse;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;

public class DeleteRemoteTrelloCardResponseFactory {

	public static Bundle parseResult(String body) {
		// TODO Auto-generated method stub
		Gson gson = new Gson();
		AuthTokenRevokedOrCardDeletedResponse deleted = gson.fromJson(body, AuthTokenRevokedOrCardDeletedResponse.class);
		Bundle bundle = new Bundle();
		if (deleted != null && deleted._value == null) {
			// TODO deleted
			bundle.putBoolean(VelloRequestFactory.BUNDLE_EXTRA_REMOTE_TRELLO_CARD_DELETED, true);
		} else {
			// TODO failed when deleting card
			bundle.putBoolean(VelloRequestFactory.BUNDLE_EXTRA_REMOTE_TRELLO_CARD_DELETED, false);
		}
		
		return bundle;
	}

}
