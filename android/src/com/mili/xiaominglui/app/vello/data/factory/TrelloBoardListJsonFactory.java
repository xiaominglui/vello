package com.mili.xiaominglui.app.vello.data.factory;

import android.os.Bundle;
import android.util.Log;

import com.foxykeep.datadroid.exception.DataException;
import com.mili.xiaominglui.app.vello.config.JSONTag;
import com.mili.xiaominglui.app.vello.data.model.Board;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TrelloBoardListJsonFactory {
	private static final String TAG = TrelloBoardListJsonFactory.class
			.getSimpleName();

	private TrelloBoardListJsonFactory() {
		// No public constructor
	}

	public static Bundle parseResult(String wsResponse) throws DataException {
		ArrayList<Board> boardList = new ArrayList<Board>();
		try {
			JSONArray jsonBoardArray = new JSONArray(wsResponse);
			int size = jsonBoardArray.length();
			for (int i = 0; i < size; i++) {
				JSONObject jsonBoard = jsonBoardArray.getJSONObject(i);
				Board board = new Board();

				board.id = jsonBoard.getString(JSONTag.BOARD_ELEM_ID);
				board.name = jsonBoard.getString(JSONTag.BOARD_ELEM_NAME);
				board.desc = jsonBoard.getString(JSONTag.BOARD_ELEM_DESC);
				board.closed = jsonBoard.getString(JSONTag.BOARD_ELEM_CLOSED);
				board.idOrganization = jsonBoard
						.getString(JSONTag.BOARD_ELEM_IDORGANIZATION);

				boardList.add(board);
			}

		} catch (JSONException e) {
			Log.e(TAG, "JSONException", e);
			throw new DataException(e);
		}

		Bundle bundle = new Bundle();
		bundle.putParcelableArrayList(
				VelloRequestFactory.BUNDLE_EXTRA_TRELLO_BOARD_LIST, boardList);
		return bundle;
	}
}
