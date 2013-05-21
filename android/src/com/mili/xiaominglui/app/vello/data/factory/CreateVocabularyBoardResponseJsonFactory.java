package com.mili.xiaominglui.app.vello.data.factory;

import android.os.Bundle;

import com.google.gson.Gson;
import com.mili.xiaominglui.app.vello.data.model.Board;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;

public class CreateVocabularyBoardResponseJsonFactory {
    private CreateVocabularyBoardResponseJsonFactory() {
	// No public constructor
    }

    public static Bundle parseResult(String wsResponse) {
	Gson gson = new Gson();
	Board board = gson.fromJson(wsResponse, Board.class);
	
	if (board != null) {
	    Bundle bundle = new Bundle();
	    bundle.putString(VelloRequestFactory.BUNDLE_EXTRA_VOCABULARY_BOARD_ID, board.id);
	    return bundle;
	}
	return null;
    }
}
