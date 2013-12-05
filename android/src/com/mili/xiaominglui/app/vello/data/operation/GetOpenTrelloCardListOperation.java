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
import com.mili.xiaominglui.app.vello.data.factory.TrelloCardListJsonFactory;
import com.mili.xiaominglui.app.vello.util.AccountUtils;

public class GetOpenTrelloCardListOperation implements Operation {
	private static final String TAG = GetOpenTrelloCardListOperation.class.getSimpleName();

	@Override
	public Bundle execute(Context context, Request request) throws ConnectionException, DataException, CustomRequestException {

		String token = AccountUtils.getAuthToken(context);
		String vocabularyBoardId = AccountUtils.getVocabularyBoardId(context);
		String urlString = WSConfig.TRELLO_API_URL + WSConfig.WS_TRELLO_TARGET_BOARD + "/" + vocabularyBoardId + WSConfig.WS_TRELLO_FIELD_CARDS;

		HashMap<String, String> parameterMap = new HashMap<String, String>();
		parameterMap.put(WSConfig.WS_TRELLO_PARAM_FILTER, "open");
		parameterMap.put(WSConfig.WS_TRELLO_PARAM_FIELDS, "name,desc,due,closed,idList,dateLastActivity");
		parameterMap.put(WSConfig.WS_TRELLO_PARAM_APP_KEY, WSConfig.VELLO_APP_KEY);
		parameterMap.put(WSConfig.WS_TRELLO_PARAM_ACCESS_TOKEN, token);

		NetworkConnection networkConnection = new NetworkConnection(context, urlString);
		networkConnection.setMethod(Method.GET);
		networkConnection.setParameters(parameterMap);
		ConnectionResult result = networkConnection.execute();

		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "result.body = " + result.body);
		}
		
		
/*
//		// Clear the table
//		context.getContentResolver().delete(DbWordCard.CONTENT_URI, null, null);

		// Adds the wordcards in the database
		int wordCardListSize = wordCardList.size();
		if (wordCardListSize > 0) {
			ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
			for (TrelloCard wordCard : wordCardList) {
				operationList.add(ContentProviderOperation
						.newInsert(DbWordCard.CONTENT_URI)
						.withValues(wordCard.toContentValues()).build());
			}

			try {
				context.getContentResolver().applyBatch(
						VelloProvider.AUTHORITY, operationList);
				bundle.putBoolean(VelloRequestFactory.BUNDLE_EXTRA_RESULT_STATUS, true);
			} catch (RemoteException e) {
				throw new DataException(e);
			} catch (OperationApplicationException e) {
				throw new DataException(e);
			}
		}
		*/

		return TrelloCardListJsonFactory.parseResult(result.body);
	}
}
