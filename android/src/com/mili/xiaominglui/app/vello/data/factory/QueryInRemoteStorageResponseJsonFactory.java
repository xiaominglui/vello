package com.mili.xiaominglui.app.vello.data.factory;

import android.os.Bundle;
import android.util.Log;

import com.foxykeep.datadroid.exception.DataException;
import com.mili.xiaominglui.app.vello.config.JSONTag;
import com.mili.xiaominglui.app.vello.data.model.TrelloCard;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class QueryInRemoteStorageResponseJsonFactory {
	private static final String TAG = QueryInRemoteStorageResponseJsonFactory.class
			.getSimpleName();

	public static Bundle parseResult(String wsResponse) throws DataException {
		ArrayList<TrelloCard> wordCardList = new ArrayList<TrelloCard>();
		try {
			JSONObject jsonSearchResult = new JSONObject(wsResponse);
			JSONArray jsonCardArray = jsonSearchResult
					.getJSONArray(JSONTag.SEARCH_ELEM_CARDS);
			int size = jsonCardArray.length();
			for (int i = 0; i < size; i++) {
				JSONObject jsonCard = jsonCardArray.getJSONObject(i);
				TrelloCard wordCard = new TrelloCard();
				wordCard.id = jsonCard.getString(JSONTag.CARD_ELEM_ID);
				wordCard.name = jsonCard.getString(JSONTag.CARD_ELEM_NAME);
				wordCard.desc = jsonCard.getString(JSONTag.CARD_ELEM_DESC);
				wordCard.due = jsonCard.getString(JSONTag.CARD_ELEM_DUE);
				wordCard.closed = jsonCard.getString(JSONTag.CARD_ELEM_CLOSED);
				wordCard.idList = jsonCard.getString(JSONTag.CARD_ELEM_IDLIST);
				wordCard.dateLastActivity = jsonCard.getString(JSONTag.CARD_ELEM_DATELASTACTIVITY);
				wordCardList.add(wordCard);
			}
		} catch (JSONException e) {
			Log.e(TAG, "JSONException", e);
			throw new DataException(e);
		}

		Bundle bundle = new Bundle();
		bundle.putParcelableArrayList(
				VelloRequestFactory.BUNDLE_EXTRA_TRELLO_CARD_LIST, wordCardList);
		return bundle;
	}

}
