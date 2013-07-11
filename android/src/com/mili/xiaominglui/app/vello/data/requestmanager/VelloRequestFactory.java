
package com.mili.xiaominglui.app.vello.data.requestmanager;

import com.foxykeep.datadroid.requestmanager.Request;
import com.mili.xiaominglui.app.vello.data.model.WordCard;

public final class VelloRequestFactory {

    // Request types
    public static final int REQUEST_TYPE_GET_DUE_WORDCARD_LIST = 0;
    public static final int REQUEST_TYPE_LOOK_UP_WORD = 1;
    public static final int REQUEST_TYPE_CHECK_VOCABULARY_BOARD = 2;
    public static final int REQUEST_TYPE_CONFIGURE_VOCABULARY_BOARD = 3;
    public static final int REQUEST_TYPE_CHECK_VOCABULARY_LIST = 4;
    public static final int REQUEST_TYPE_REOPEN_VOCABULARY_LIST = 5;
    public static final int REQUEST_TYPE_CREATE_VOCABULARY_LIST = 6;
    public static final int REQUEST_TYPE_CREATE_VOCABULARY_BOARD = 7;
    public static final int REQUEST_TYPE_CHECK_WORDCARD_STATUS = 8;
    public static final int REQUEST_TYPE_ADD_WORDCARD = 9;
    public static final int REQUEST_TYPE_REOPEN_WORDCARD = 10;
    public static final int REQUEST_TYPE_INITIALIZE_WORDCARD = 11;
    public static final int REQUEST_TYPE_REVIEWED_WORDCARD = 12;
    public static final int REQUEST_TYPE_REVIEWED_PLUS_WORDCARD = 13;
    public static final int REQUEST_TYPE_ARCHIVE_WORDCARD = 14;
    public static final int REQUEST_TYPE_UPGRADE_WORDCARD = 15;
    public static final int REQUEST_TYPE_QUERY_IN_LOCAL_CACHE = 16;

    // Response data
    public static final String BUNDLE_EXTRA_TRELLO_BOARD_LIST = "com.mili.xiaominglui.app.vello.extra.boardList";
    public static final String BUNDLE_EXTRA_WORDCARD_LIST = "com.mili.xiaominglui.app.vello.extra.wordCardList";
    public static final String BUNDLE_EXTRA_WORDCARD = "com.mili.xiaominglui.app.vello.extra.wordCard";
    public static final String BUNDLE_EXTRA_WORDLIST = "com.mili.xiaominglui.app.vello.extra.wordList";
    public static final String BUNDLE_EXTRA_VOCABULARY_BOARD_ID = "com.mili.xiaominglui.app.vello.extra.boardId";
    public static final String BUNDLE_EXTRA_VOCABULARY_LIST_LIST = "com.mili.xiaominglui.app.vello.extra.listList";
    public static final String BUNDLE_EXTRA_VOCABULARY_LIST_ID = "com.mili.xiaominglui.app.vello.extra.listId";
    public static final String BUNDLE_EXTRA_DICTIONARY_ICIBA_RESPONSE = "com.mili.xiaominglui.app.vello.extra.iciba";
    public static final String BUNDLE_EXTRA_RESULT_STATUS = "com.mili.xiaominglui.app.vello.extra.status";

    // Parameter data
    public static final String PARAM_EXTRA_VOCABULARY_BOARD_ID = "com.mili.xiaominglui.app.vello.extra.boardId";
    public static final String PARAM_EXTRA_VOCABULARY_LIST_POSITION = "com.mili.xiaominglui.app.vello.extra.listPosition";
    public static final String PARAM_EXTRA_VOCABULARY_LIST_ID = "com.mili.xiaominglui.app.vello.extra.listId";
    public static final String PARAM_EXTRA_VOCABULARY_CARD_ID = "com.mili.xiaominglui.app.vello.extra.cardId";
    public static final String PARAM_EXTRA_QUERY_WORD_KEYWORD = "com.mili.xiaominglui.app.vello.extra.keyword";
    public static final String PARAM_EXTRA_CHECK_WORDCARD_WS_RESULT = "com.mili.xiaominglui.app.vello.extra.ws.result";
    public static final String PARAM_EXTRA_DATE_LAST_ACTIVITY = "com.mili.xiaominglui.app.vello.extra.dateLastActivity";

    private VelloRequestFactory() {
        // no public constructor
    }

    public static Request getDueWordCardListRequest() {
        Request request = new Request(REQUEST_TYPE_GET_DUE_WORDCARD_LIST);
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

    public static Request lookUpWordRequest(String query) {
        Request request = new Request(REQUEST_TYPE_LOOK_UP_WORD);
        request.put(PARAM_EXTRA_QUERY_WORD_KEYWORD, query);
        request.setMemoryCacheEnabled(true);
        return request;
    }

    public static Request checkWordCardStatusRequest(String keyword, String wsResult) {
        Request request = new Request(REQUEST_TYPE_CHECK_WORDCARD_STATUS);
        request.put(PARAM_EXTRA_QUERY_WORD_KEYWORD, keyword);
        request.put(PARAM_EXTRA_CHECK_WORDCARD_WS_RESULT, wsResult);
        request.setMemoryCacheEnabled(true);
        return request;
    }

    public static Request addWordCardRequest(String keyword, String wsResult) {
        Request request = new Request(REQUEST_TYPE_ADD_WORDCARD);
        request.put(PARAM_EXTRA_QUERY_WORD_KEYWORD, keyword);
        request.put(PARAM_EXTRA_CHECK_WORDCARD_WS_RESULT, wsResult);
        request.setMemoryCacheEnabled(true);
        return request;
    }

    public static Request reOpenWordCardRequest(String idCard) {
        Request request = new Request(REQUEST_TYPE_REOPEN_WORDCARD);
        request.put(PARAM_EXTRA_VOCABULARY_CARD_ID, idCard);
        request.setMemoryCacheEnabled(true);
        return request;
    }

    public static Request initializeWordCardRequest(String idCard) {
        Request request = new Request(REQUEST_TYPE_INITIALIZE_WORDCARD);
        request.put(PARAM_EXTRA_VOCABULARY_CARD_ID, idCard);
        request.setMemoryCacheEnabled(true);
        return request;
    }

    public static Request reviewedWordCardRequest(String idCard, int position) {
        Request request = new Request(REQUEST_TYPE_REVIEWED_WORDCARD);
        request.put(PARAM_EXTRA_VOCABULARY_CARD_ID, idCard);
        request.put(PARAM_EXTRA_VOCABULARY_LIST_POSITION, position);
        request.setMemoryCacheEnabled(true);
        return request;
    }
    
    public static Request reviewedPlusWordCardRequest(String idCard) {
        Request request = new Request(REQUEST_TYPE_REVIEWED_PLUS_WORDCARD);
        request.put(PARAM_EXTRA_VOCABULARY_CARD_ID, idCard);
        request.setMemoryCacheEnabled(false);
        return request;
    }
    
	public static Request archiveWordCardRequest(String idCard) {
		Request request = new Request(REQUEST_TYPE_ARCHIVE_WORDCARD);
		request.put(PARAM_EXTRA_VOCABULARY_CARD_ID, idCard);
		request.setMemoryCacheEnabled(true);
		return request;
	}
	
	public static Request queryInLocalCacheRequest(String query) {
	    Request request = new Request(REQUEST_TYPE_QUERY_IN_LOCAL_CACHE);
	    request.put(PARAM_EXTRA_QUERY_WORD_KEYWORD, query);
	    request.setMemoryCacheEnabled(true);
	    return request;
	}
}
