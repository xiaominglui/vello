package com.mili.xiaominglui.app.vello.data.operation;

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
import com.mili.xiaominglui.app.vello.data.factory.CreateVocabularyListResponseJsonFactory;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;
import com.mili.xiaominglui.app.vello.util.AccountUtils;

import java.util.HashMap;

public class CreateVocabularyListOperation implements Operation {
    private static final String TAG = CreateVocabularyBoardOperation.class.getSimpleName();

    @Override
    public Bundle execute(Context context, Request request)
	    throws ConnectionException, DataException, CustomRequestException {
	String token = AccountUtils.getAuthToken(context);
	String vocabularyBoardId = AccountUtils.getVocabularyBoardId(context);
	int position = request.getInt(VelloRequestFactory.PARAM_EXTRA_VOCABULARY_LIST_POSITION);
	
	String urlString = WSConfig.TRELLO_API_URL + WSConfig.WS_TRELLO_TARGET_LIST;
	
	HashMap<String, String> parameterMap = new HashMap<String, String>();
	parameterMap.put(WSConfig.WS_TRELLO_PARAM_NAME, AccountUtils.VOCABULARY_LISTS_TITLE_ID[position]);
	parameterMap.put(WSConfig.WS_TRELLO_PARAM_IDBOARD, vocabularyBoardId);
	parameterMap.put(WSConfig.WS_TRELLO_PARAM_APP_KEY, WSConfig.VELLO_APP_KEY);
	parameterMap.put(WSConfig.WS_TRELLO_PARAM_ACCESS_TOKEN, token);
	
	NetworkConnection networkConnection = new NetworkConnection(context,
		urlString);
	networkConnection.setMethod(Method.POST);
	networkConnection.setParameters(parameterMap);
	ConnectionResult result = networkConnection.execute();
	
	if (VelloConfig.DEBUG_SWITCH) {
	    Log.d(TAG, "result.body = " + result.body);
	}
	
	return CreateVocabularyListResponseJsonFactory.parseResult(result.body);
    }

}
