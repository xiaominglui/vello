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
import com.mili.xiaominglui.app.vello.data.factory.CheckTrelloConnectionResponseFactory;

public class CheckTrelloConnectionOperation implements Operation {
	private static final String TAG = CheckTrelloConnectionOperation.class
			.getSimpleName();

	@Override
	public Bundle execute(Context context, Request request)
			throws ConnectionException, DataException, CustomRequestException {
		// TODO Auto-generated method stub
		String urlString = WSConfig.TRELLO_API_URL;
		NetworkConnection networkConnection = new NetworkConnection(context,
				urlString);
		networkConnection.setMethod(Method.GET);
		ConnectionResult result = networkConnection.execute();
		
		if (VelloConfig.DEBUG_SWITCH) {
		    Log.d(TAG, "result.body = " + result.body);
		}

		return CheckTrelloConnectionResponseFactory.parseResult(result.body);
	}

}
