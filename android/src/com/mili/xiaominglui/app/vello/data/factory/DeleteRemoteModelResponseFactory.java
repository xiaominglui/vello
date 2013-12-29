package com.mili.xiaominglui.app.vello.data.factory;

import android.os.Bundle;

import com.google.gson.Gson;
import com.mili.xiaominglui.app.vello.data.model.RemoteModelDeletedResponse;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;

public class DeleteRemoteModelResponseFactory {

	public static Bundle parseResult(String body) {
		Gson gson = new Gson();
		RemoteModelDeletedResponse deleted = gson.fromJson(body, RemoteModelDeletedResponse.class);
		Bundle bundle = new Bundle();
		if (deleted != null && deleted._value == null) {
			// deleted
			bundle.putBoolean(VelloRequestFactory.BUNDLE_EXTRA_REMOTE_MODEL_DELETED, true);
		} else {
			// failed when deleting
			bundle.putBoolean(VelloRequestFactory.BUNDLE_EXTRA_REMOTE_MODEL_DELETED, false);
		}
		return bundle;
	}
}
