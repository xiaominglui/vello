package com.mili.xiaominglui.app.vello.data.operation;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
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
import com.mili.xiaominglui.app.vello.data.factory.AllWordCardListJsonFactory;
import com.mili.xiaominglui.app.vello.data.model.WordCard;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbWordCard;
import com.mili.xiaominglui.app.vello.data.provider.util.ProviderCriteria;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;
import com.mili.xiaominglui.app.vello.util.AccountUtils;

public class SyncLocalCacheOperation implements Operation {
	private static final String TAG = SyncLocalCacheOperation.class.getSimpleName();

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
		wordCardList = AllWordCardListJsonFactory.parseResult(result.body);
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "result.body = " + result.body);
		}
		
		Bundle bundle = new Bundle();
		bundle.putParcelableArrayList(VelloRequestFactory.BUNDLE_EXTRA_WORDCARD_LIST, wordCardList);
		
		return bundle;
	}
	
	private ArrayList<WordCard> queryForRemoteOpenWordCardList(Context context) {
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
        
        ConnectionResult result;
		try {
			result = networkConnection.execute();
			ArrayList<WordCard> wordCardList = new ArrayList<WordCard>();
	        wordCardList = AllWordCardListJsonFactory.parseResult(result.body);
	        return wordCardList;
		} catch (ConnectionException e) {
			e.printStackTrace();
		} catch (DataException e) {
			e.printStackTrace();
		}
        return null;
    }

}
