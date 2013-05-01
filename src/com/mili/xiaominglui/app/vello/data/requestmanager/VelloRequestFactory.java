package com.mili.xiaominglui.app.vello.data.requestmanager;

import android.os.Parcel;

import com.foxykeep.datadroid.requestmanager.Request;
import com.mili.xiaominglui.app.vello.data.operation.CheckVocabularyBoardOperation;
import com.mili.xiaominglui.app.vello.data.operation.WordListOperation;

public final class VelloRequestFactory {
    
    // Request types
    public static final int REQUEST_TYPE_QUERY_WORD_LIST_DOING = 0;
    public static final int REQUEST_TYPE_QUERY_WORD = 1;
    public static final int REQUEST_TYPE_CHECK_VOCABULARY_BOARD = 2;
    public static final int REQUEST_TYPE_CONFIGURE_VOCABULARY_BOARD = 3;
    public static final int REQUEST_TYPE_CHECK_VOCABULARY_LIST = 4;
    public static final int REQUEST_TYPE_REOPEN_VOCABULARY_LIST = 5;
    public static final int REQUEST_TYPE_CREATE_VOCABULARY_LIST = 6;
    public static final int REQUEST_TYPE_CREATE_VOCABULARY_BOARD = 7;
    
    // Response data
    public static final String BUNDLE_EXTRA_TRELLO_BOARD_LIST = "com.mili.xiaominglui.app.vello.extra.boardList";
    public static final String BUNDLE_EXTRA_VOCABULARY_BOARD_ID = "com.mili.xiaominglui.app.vello.extra.boardId";
    public static final String BUNDLE_EXTRA_VOCABULARY_LIST_LIST = "com.mili.xiaominglui.app.vello.extra.listList";
    public static final String BUNDLE_EXTRA_VOCABULARY_LIST_ID = "com.mili.xiaominglui.app.vello.extra.listId";
    
    // Parameter data
    public static final String PARAM_EXTRA_VOCABULARY_BOARD_ID = "com.mili.xiaominglui.app.vello.extra.boardId";
    public static final String PARAM_EXTRA_VOCABULARY_LIST_POSITION = "com.mili.xiaominglui.app.vello.extra.listPosition";
    public static final String PARAM_EXTRA_VOCABULARY_LIST_ID = "com.mili.xiaominglui.app.vello.extra.listId";
    
    private VelloRequestFactory() {
	// no public constructor
    }
    
    public static Request getMyDoingWordListRequest(int returnFormat) {
	Request request = new Request(REQUEST_TYPE_QUERY_WORD_LIST_DOING);
	request.put(WordListOperation.PARAM_RETURN_FORMAT, returnFormat);
	return request;
    }
    
    public static Request checkVocabularyBoardRequest() {
	Request request = new Request(REQUEST_TYPE_CHECK_VOCABULARY_BOARD);
	request.setMemoryCacheEnabled(true);
	return request;
    }
    
    public static Request configureVocabularyBoardRequest(String id) {
	Request request = new Request(REQUEST_TYPE_CONFIGURE_VOCABULARY_BOARD);
	request.setMemoryCacheEnabled(true);
	request.put(PARAM_EXTRA_VOCABULARY_BOARD_ID, id);
	return request;
    }
    
    public static Request checkVocabularyListRequest(int position) {
	Request request = new Request(REQUEST_TYPE_CHECK_VOCABULARY_LIST);
	request.put(PARAM_EXTRA_VOCABULARY_LIST_POSITION, position);
	request.setMemoryCacheEnabled(true);
	return request;
    }
    
    public static Request reOpenVocabularyListRequest(int position, String id) {
	Request request = new Request(REQUEST_TYPE_REOPEN_VOCABULARY_LIST);
	request.put(PARAM_EXTRA_VOCABULARY_LIST_ID, id);
	request.put(PARAM_EXTRA_VOCABULARY_LIST_POSITION, position);
	request.setMemoryCacheEnabled(true);
	return request;
    }
    
    public static Request createVocabularyListRequest(int position) {
	Request request = new Request(REQUEST_TYPE_CREATE_VOCABULARY_LIST);
	request.put(PARAM_EXTRA_VOCABULARY_LIST_POSITION, position);
	request.setMemoryCacheEnabled(true);
	return request;
    }
    
    public static Request createVocabularyBoardRequest() {
	Request request = new Request(REQUEST_TYPE_CREATE_VOCABULARY_BOARD);
	request.setMemoryCacheEnabled(true);
	return request;
    }

}
