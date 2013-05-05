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
import com.mili.xiaominglui.app.vello.data.factory.IcibaDictionaryResponseXmlFactory;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;

import java.util.HashMap;

public class QueryWordOperation implements Operation {

    @Override
    public Bundle execute(Context context, Request request)
	    throws ConnectionException, DataException, CustomRequestException {
	String keyword = request.getString(VelloRequestFactory.PARAM_EXTRA_QUERY_WORD_KEYWORD);
	
	String urlString = WSConfig.WS_DICTIONARY_ICIBA_API;
	
	HashMap<String, String> parameterMap = new HashMap<String, String>();
	parameterMap.put(WSConfig.WS_DICTIONARY_ICIBA_PARAM_KEYWORD, keyword);
	
	NetworkConnection networkConnection = new NetworkConnection(context,
		urlString);
	networkConnection.setMethod(Method.GET);
	networkConnection.setParameters(parameterMap);
	ConnectionResult result = networkConnection.execute();
	return IcibaDictionaryResponseXmlFactory.parseResult(result.body);
    }

}
