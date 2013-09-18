package com.mili.xiaominglui.app.vello.data.operation;

import java.util.HashMap;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.foxykeep.datadroid.exception.ConnectionException;
import com.foxykeep.datadroid.exception.CustomRequestException;
import com.foxykeep.datadroid.exception.DataException;
import com.foxykeep.datadroid.network.NetworkConnection;
import com.foxykeep.datadroid.network.NetworkConnection.ConnectionResult;
import com.foxykeep.datadroid.network.NetworkConnection.Method;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.service.RequestService.Operation;
import com.mili.xiaominglui.app.vello.config.VelloConfig;
import com.mili.xiaominglui.app.vello.config.WSConfig;
import com.mili.xiaominglui.app.vello.data.factory.SetWebHookActiveResponseJsonFactory;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;
import com.mili.xiaominglui.app.vello.util.AccountUtils;

public class SetWebHookActiveOperation implements Operation {
	private static final String TAG = SetWebHookActiveOperation.class.getSimpleName();

	@Override
	public Bundle execute(Context context, Request request)
			throws ConnectionException, DataException, CustomRequestException {
		String token = AccountUtils.getAuthToken(context);
		String idWebhook = AccountUtils.getVocabularyBoardWebHookId(context);
		String active = request.getBooleanAsString(VelloRequestFactory.PARAM_EXTRA_WEBHOOK_ACTIVE);
		if (token.equals("") || idWebhook.equals("")) {
			Log.i(TAG, "not both token and idWebhook exist");
			return null;
		}
		
		String urlString = WSConfig.TRELLO_API_URL + WSConfig.WS_TRELLO_TARGET_WEBHOOKS + "/" + idWebhook;
		
		HashMap<String, String> parameterMap = new HashMap<String, String>();
		parameterMap.put(WSConfig.WS_TRELLO_PARAM_WEBHOOK_ACTIVE, active);
		parameterMap.put(WSConfig.WS_TRELLO_PARAM_APP_KEY, WSConfig.VELLO_APP_KEY);
		parameterMap.put(WSConfig.WS_TRELLO_PARAM_ACCESS_TOKEN, token);
		
		NetworkConnection networkConnection = new NetworkConnection(context,
				urlString);
		networkConnection.setMethod(Method.PUT);
		networkConnection.setParameters(parameterMap);
		ConnectionResult result = networkConnection.execute();
		
		if (VelloConfig.DEBUG_SWITCH) {
		    Log.d(TAG, "result.body = " + result.body);
		}
		return SetWebHookActiveResponseJsonFactory.parseResult(result.body);
	}
}
