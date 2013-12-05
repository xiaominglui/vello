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

public class TrelloCardListJsonFactory {
	private static final String TAG = TrelloCardListJsonFactory.class.getSimpleName();

	public static Bundle parseResult(String wsResponse) throws DataException {
		ArrayList<TrelloCard> trelloCardList = new ArrayList<TrelloCard>();

		try {
			JSONArray jsonCardArray = new JSONArray(wsResponse);
			int size = jsonCardArray.length();
			for (int i = 0; i < size; i++) {
				JSONObject jsonCard = jsonCardArray.getJSONObject(i);
				TrelloCard trelloCard = new TrelloCard();
				trelloCard.id = jsonCard.getString(JSONTag.CARD_ELEM_ID);
				trelloCard.name = jsonCard.getString(JSONTag.CARD_ELEM_NAME);
				trelloCard.desc = jsonCard.getString(JSONTag.BOARD_ELEM_DESC);
				trelloCard.due = jsonCard.getString(JSONTag.CARD_ELEM_DUE);
				trelloCard.closed = jsonCard.getString(JSONTag.CARD_ELEM_CLOSED);
				trelloCard.idList = jsonCard.getString(JSONTag.CARD_ELEM_IDLIST);
				trelloCard.dateLastActivity = jsonCard.getString(JSONTag.CARD_ELEM_DATELASTACTIVITY);
				trelloCardList.add(trelloCard);
			}
		} catch (JSONException e) {
			Log.e(TAG, "JSONException", e);
			throw new DataException(e);
		}
		Bundle bundle = new Bundle();
		bundle.putParcelableArrayList(VelloRequestFactory.BUNDLE_EXTRA_TRELLO_CARD_LIST, trelloCardList);

		return bundle;
	}
}
