package com.mili.xiaominglui.app.vello.data.factory;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.foxykeep.datadroid.exception.DataException;
import com.mili.xiaominglui.app.vello.config.JSONTag;
import com.mili.xiaominglui.app.vello.data.model.WordCard;

public class SyncTrelloDBResponseJsonFactory {
	private static final String TAG = SyncTrelloDBResponseJsonFactory.class
			.getSimpleName();

	public static ArrayList<WordCard> parseResult(String wsResponse)
			throws DataException {
		ArrayList<WordCard> wordCardList = new ArrayList<WordCard>();

		try {
			JSONArray jsonCardArray = new JSONArray(wsResponse);
			int size = jsonCardArray.length();
			for (int i = 0; i < size; i++) {
				JSONObject jsonCard = jsonCardArray.getJSONObject(i);
				WordCard wordCard = new WordCard();
				wordCard.idCard = jsonCard.getString(JSONTag.CARD_ELEM_ID);
				wordCard.name = jsonCard.getString(JSONTag.CARD_ELEM_NAME);
				wordCard.desc = jsonCard.getString(JSONTag.BOARD_ELEM_DESC);
				wordCard.due = jsonCard.getString(JSONTag.CARD_ELEM_DUE);
				wordCard.idList = jsonCard.getString(JSONTag.CARD_ELEM_IDLIST);
				wordCard.dateLastActivity = jsonCard
						.getString(JSONTag.CARD_ELEM_DATELASTACTIVITY);
				wordCardList.add(wordCard);
			}
		} catch (JSONException e) {
			Log.e(TAG, "JSONException", e);
			throw new DataException(e);
		}

		return wordCardList;
	}

}
