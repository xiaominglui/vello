package com.mili.xiaominglui.app.vello.data.operation;

import java.util.HashMap;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.avos.avoscloud.ParseInstallation;
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
import com.mili.xiaominglui.app.vello.data.factory.CreateVocabularyListResponseJsonFactory;
import com.mili.xiaominglui.app.vello.data.factory.CreateWebHooksResponseJsonFactory;
import com.mili.xiaominglui.app.vello.util.AccountUtils;

public class CreateWebHooksOperation implements Operation {
	private static final String TAG = CreateWebHooksOperation.class.getSimpleName();

	@Override
	public Bundle execute(Context context, Request request)
			throws ConnectionException, DataException, CustomRequestException {
		// TODO Auto-generated method stub
		String token = AccountUtils.getAuthToken(context);
		String vocabularyBoardId = AccountUtils.getVocabularyBoardId(context);
		
		String urlString = WSConfig.TRELLO_API_URL + WSConfig.WS_TRELLO_TARGET_TOKEN + "/" + token + "/webhooks/";
		String myCallback = WSConfig.WS_AVELLO_TRELISTENER_URL + "?oid=" + ParseInstallation.getCurrentInstallation().getObjectId();
		
		HashMap<String, String> parameterMap = new HashMap<String, String>();
		parameterMap.put(WSConfig.WS_TRELLO_PARAM_APP_KEY, WSConfig.VELLO_APP_KEY);
		parameterMap.put(WSConfig.WS_TRELLO_PARAM_WEBHOOK_DESCRIPTION, VelloConfig.DEFAULT_VOCABULARY_BOARD_HOOK_DESCRIPTION);
		parameterMap.put(WSConfig.WS_TRELLO_PARAM_WEBHOOK_CALLBACK_URL, myCallback);
		parameterMap.put(WSConfig.WS_TRELLO_PARAM_WEBHOOK_ID_MODEL, vocabularyBoardId);
		
		NetworkConnection networkConnection = new NetworkConnection(context,
				urlString);
		networkConnection.setMethod(Method.POST);
		networkConnection.setParameters(parameterMap);
		ConnectionResult result = networkConnection.execute();
		
		if (VelloConfig.DEBUG_SWITCH) {
		    Log.d(TAG, "result.body = " + result.body);
		}
		
		
		return CreateWebHooksResponseJsonFactory.parseResult(result.body);
	}

}
