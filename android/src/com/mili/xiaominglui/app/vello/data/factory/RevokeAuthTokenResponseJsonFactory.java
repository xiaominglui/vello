package com.mili.xiaominglui.app.vello.data.factory;

import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.mili.xiaominglui.app.vello.config.VelloConfig;
import com.mili.xiaominglui.app.vello.data.model.AuthTokenRevokedResponse;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;

public class RevokeAuthTokenResponseJsonFactory {
	private static final String TAG = RevokeAuthTokenResponseJsonFactory.class.getSimpleName();

	public static Bundle parseResult(String wsResponse) {
		Gson gson = new Gson();
		AuthTokenRevokedResponse revoked = gson.fromJson(wsResponse, AuthTokenRevokedResponse.class);
		Bundle bundle = new Bundle();
		if (revoked != null && revoked._value == null) {
			// revoked
			if (VelloConfig.DEBUG_SWITCH) {
				Log.d(TAG, "auth token has been revoked");
			}
			bundle.putBoolean(VelloRequestFactory.BUNDLE_EXTRA_HAS_AUTH_TOKEN_REVOKED, true);
		}
		return bundle;
	}
}
