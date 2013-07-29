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
import com.mili.xiaominglui.app.vello.data.factory.AddWordCardResponseJsonFactory;
import com.mili.xiaominglui.app.vello.data.factory.QueryInRemoteStorageResponseJsonFactory;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;
import com.mili.xiaominglui.app.vello.util.AccountUtils;

import java.util.HashMap;

public class AddWordCardOperation implements Operation {
    private static final String TAG = AddWordCardOperation.class.getSimpleName();

    @Override
    public Bundle execute(Context context, Request request)
	    throws ConnectionException, DataException, CustomRequestException {
	String token = AccountUtils.getAuthToken(context);
	String keyword = request.getString(VelloRequestFactory.PARAM_EXTRA_QUERY_WORD_KEYWORD);
	String data = request.getString(VelloRequestFactory.PARAM_EXTRA_DICTIONARY_WS_RESULT);
	String idList = AccountUtils.getVocabularyListId(context, VelloConfig.VOCABULARY_LIST_POSITION_NEW);
	
	String urlString = WSConfig.TRELLO_API_URL + WSConfig.WS_TRELLO_TARGET_CARD;
	
	HashMap<String, String> parameterMap = new HashMap<String, String>();
	parameterMap.put(WSConfig.WS_TRELLO_PARAM_NAME, keyword);
	parameterMap.put(WSConfig.WS_TRELLO_PARAM_DESC, data);
	parameterMap.put(WSConfig.WS_TRELLO_PARAM_IDLIST, idList);
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
	
	return AddWordCardResponseJsonFactory.parseResult(result.body);
    }

}
