package com.mili.xiaominglui.app.vello.data.operation;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.Bundle;
import android.os.RemoteException;
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
import com.mili.xiaominglui.app.vello.data.factory.WordCardListJsonFactory;
import com.mili.xiaominglui.app.vello.data.factory.DueWordCardListJsonFactory;
import com.mili.xiaominglui.app.vello.data.model.WordCard;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbWordCard;
import com.mili.xiaominglui.app.vello.data.provider.VelloProvider;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;
import com.mili.xiaominglui.app.vello.util.AccountUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class GetDueWordCardListOperation implements Operation {
	private static final String TAG = GetDueWordCardListOperation.class
			.getSimpleName();

	@Override
	public Bundle execute(Context context, Request request)
			throws ConnectionException, DataException, CustomRequestException {

		String token = AccountUtils.getAuthToken(context);
		String vocabularyBoardId = AccountUtils.getVocabularyBoardId(context);
		String urlString = WSConfig.TRELLO_API_URL
				+ WSConfig.WS_TRELLO_TARGET_BOARD + "/" + vocabularyBoardId
				+ WSConfig.WS_TRELLO_FIELD_CARDS;

		HashMap<String, String> parameterMap = new HashMap<String, String>();
		parameterMap.put(WSConfig.WS_TRELLO_PARAM_FILTER, "open");
		parameterMap.put(WSConfig.WS_TRELLO_PARAM_FIELDS,
				"name,desc,due,closed,idList,dateLastActivity");
		parameterMap.put(WSConfig.WS_TRELLO_PARAM_APP_KEY,
				WSConfig.VELLO_APP_KEY);
		parameterMap.put(WSConfig.WS_TRELLO_PARAM_ACCESS_TOKEN, token);

		NetworkConnection networkConnection = new NetworkConnection(context,
				urlString);
		networkConnection.setMethod(Method.GET);
		networkConnection.setParameters(parameterMap);
		ConnectionResult result = networkConnection.execute();

		ArrayList<WordCard> wordCardList = new ArrayList<WordCard>();
		wordCardList = WordCardListJsonFactory.parseResult(result.body);
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "result.body = " + result.body);
		}

		// Clear the table
		context.getContentResolver().delete(DbWordCard.CONTENT_URI, null, null);

		// Adds the wordcards in the database
		int wordCardListSize = wordCardList.size();
		Bundle bundle = new Bundle();
		if (wordCardListSize > 0) {
			ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
			for (WordCard wordCard : wordCardList) {
				operationList.add(ContentProviderOperation
						.newInsert(DbWordCard.CONTENT_URI)
						.withValues(wordCard.toContentVaalues()).build());
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

		return bundle;
	}
}
