package com.mili.xiaominglui.app.vello.data.operation;



import android.content.Context;
import android.os.Bundle;

import com.foxykeep.datadroid.exception.ConnectionException;
import com.foxykeep.datadroid.exception.CustomRequestException;
import com.foxykeep.datadroid.exception.DataException;
import com.foxykeep.datadroid.network.NetworkConnection;
import com.foxykeep.datadroid.network.NetworkConnection.ConnectionResult;
import com.foxykeep.datadroid.network.NetworkConnection.Method;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.service.RequestService.Operation;
import com.mili.xiaominglui.app.vello.config.WSConfig;
import com.mili.xiaominglui.app.vello.data.factory.TrelloBoardListJsonFactory;
import com.mili.xiaominglui.app.vello.util.AccountUtils;

import java.util.HashMap;

public class CheckVocabularyBoardOperation implements Operation {

    @Override
    public Bundle execute(Context context, Request request)
	    throws ConnectionException, DataException, CustomRequestException {
	String token = AccountUtils.getAuthToken(context);
	HashMap<String, String> parameterMap = new HashMap<String, String>();
	parameterMap.put(WSConfig.WS_TRELLO_PARAM_FILTER, "all");
	parameterMap.put(WSConfig.WS_TRELLO_PARAM_FIELDS, "name,desc,closed,idOrganization");
	parameterMap.put(WSConfig.WS_TRELLO_PARAM_APP_KEY, WSConfig.VELLO_APP_KEY);
	parameterMap.put(WSConfig.WS_TRELLO_PARAM_ACCESS_TOKEN, token);

	String urlString = WSConfig.TRELLO_API_URL + WSConfig.GET_MY_BOARD_LIST;
	
	NetworkConnection networkConnection = new NetworkConnection(context,
		urlString);
	networkConnection.setMethod(Method.GET);
	networkConnection.setParameters(parameterMap);
	ConnectionResult result = networkConnection.execute();
	return TrelloBoardListJsonFactory.parseResult(result.body);
    }
}
