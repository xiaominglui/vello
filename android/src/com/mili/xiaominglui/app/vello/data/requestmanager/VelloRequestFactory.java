
package com.mili.xiaominglui.app.vello.data.requestmanager;

import com.foxykeep.datadroid.requestmanager.Request;
import com.mili.xiaominglui.app.vello.data.model.TrelloCard;

public final class VelloRequestFactory {

    // Request types
    public static final int REQUEST_TYPE_GET_OPEN_TRELLO_CARD_LIST = 0;
    public static final int REQUEST_TYPE_QUERY_IN_REMOTE_STORAGE = 1;
    public static final int REQUEST_TYPE_CHECK_VOCABULARY_BOARD = 2;
    public static final int REQUEST_TYPE_CONFIGURE_VOCABULARY_BOARD = 3;
    public static final int REQUEST_TYPE_CHECK_VOCABULARY_LIST = 4;
    public static final int REQUEST_TYPE_REOPEN_VOCABULARY_LIST = 5;
    public static final int REQUEST_TYPE_CREATE_VOCABULARY_LIST = 6;
    public static final int REQUEST_TYPE_CREATE_VOCABULARY_BOARD = 7;
    public static final int REQUEST_TYPE_ADD_WORDCARD = 8;
    public static final int REQUEST_TYPE_RESTART_WORDCARD = 9;
    public static final int REQUEST_TYPE_INITIALIZE_WORDCARD = 10;
    public static final int REQUEST_TYPE_REVIEWED_WORDCARD = 11;
    public static final int REQUEST_TYPE_ARCHIVE_WORDCARD = 13;
    public static final int REQUEST_TYPE_UPGRADE_WORDCARD = 14;
    public static final int REQUEST_TYPE_LOOK_UP_IN_DICTIONARY = 16;
    public static final int REQUEST_TYPE_CREATE_WEBHOOK = 17;
    public static final int REQUEST_TYPE_DELETE_WEBHOOK = 19;
    public static final int REQUEST_TYPE_CHECK_TRELLO_CONNECTION = 20;
    public static final int REQUEST_TYPE_READ_TRELLO_ACCOUNT_USERNAME = 21;
    public static final int REQUEST_TYPE_DELETE_REMOTE_TRELLO_CARD = 23;
    public static final int REQUEST_TYPE_UPDATE_REMOTE_TRELLO_CARD = 24;

    // Response data
    public static final String BUNDLE_EXTRA_TRELLO_BOARD_LIST = "com.mili.xiaominglui.app.vello.extra.trello.board.list";
    public static final String BUNDLE_EXTRA_TRELLO_CARD_LIST = "com.mili.xiaominglui.app.vello.extra.trello.card.list";
    public static final String BUNDLE_EXTRA_REVIEW_CARD_LIST = "com.mili.xiaominglui.app.vello.extra.review.card.list";
    public static final String BUNDLE_EXTRA_DIRTY_CARD_LIST = "com.mili.xiaominglui.app.vello.extra.dirty.card.list";
    public static final String BUNDLE_EXTRA_WORDCARD = "com.mili.xiaominglui.app.vello.extra.wordCard";
    public static final String BUNDLE_EXTRA_WORDLIST = "com.mili.xiaominglui.app.vello.extra.wordList";
    public static final String BUNDLE_EXTRA_VOCABULARY_BOARD_ID = "com.mili.xiaominglui.app.vello.extra.boardId";
    public static final String BUNDLE_EXTRA_VOCABULARY_LIST_LIST = "com.mili.xiaominglui.app.vello.extra.listList";
    public static final String BUNDLE_EXTRA_VOCABULARY_LIST_ID = "com.mili.xiaominglui.app.vello.extra.listId";
    public static final String BUNDLE_EXTRA_DICTIONARY_WS_RESPONSE = "com.mili.xiaominglui.app.vello.extra.ws.dictionary";
    public static final String BUNDLE_EXTRA_RESULT_STATUS = "com.mili.xiaominglui.app.vello.extra.status";
    public static final String BUNDLE_EXTRA_WEBHOOK_ID = "com.mili.xiaominglui.app.vello.extra.webhook.id";
    public static final String BUNDLE_EXTRA_HAS_AUTH_TOKEN_REVOKED = "com.mili.xiaominglui.app.vello.extra.token_revoked";
    public static final String BUNDLE_EXTRA_REMOTE_MODEL_DELETED = "com.mili.xiaominglui.app.vello.extra.trello.model.deleted";
    public static final String BUNDLE_EXTRA_REMOTE_TRELLO_CARD_UPDATED = "com.mili.xiaominglui.app.vello.extra.trello.card.deleted";
    public static final String BUNDLE_EXTRA_TRELLO_CONNECTION = "com.mili.xiaominglui.app.vello.extra.trello.connection";
    public static final String BUNDLE_EXTRA_TRELLO_ACCOUNT_USERNAME = "com.mili.xiaominglui.app.vello.extra.trello.username";

    // Parameter data
    public static final String PARAM_EXTRA_VOCABULARY_BOARD_ID = "com.mili.xiaominglui.app.vello.extra.boardId";
    public static final String PARAM_EXTRA_VOCABULARY_LIST_POSITION = "com.mili.xiaominglui.app.vello.extra.listPosition";
    public static final String PARAM_EXTRA_VOCABULARY_LIST_ID = "com.mili.xiaominglui.app.vello.extra.listId";
    public static final String PARAM_EXTRA_VOCABULARY_CARD_ID = "com.mili.xiaominglui.app.vello.extra.cardId";
    public static final String PARAM_EXTRA_QUERY_WORD_KEYWORD = "com.mili.xiaominglui.app.vello.extra.keyword";
    public static final String PARAM_EXTRA_DICTIONARY_WS_RESULT = "com.mili.xiaominglui.app.vello.extra.ws.result";
    public static final String PARAM_EXTRA_DATE_LAST_ACTIVITY = "com.mili.xiaominglui.app.vello.extra.dateLastActivity";
    public static final String PARAM_EXTRA_TRELLO_ACCESS_TOKEN = "com.mili.xiaominglui.app.vello.extra.trello.token";
    public static final String PARAM_EXTRA_SERVICE_START_ID = "com.mili.xiaominglui.app.vello.extra.service.startid";
    public static final String PARAM_EXTRA_DIRTY_CARD = "com.mili.xiaominglui.app.vello.extra.dirty.card";
    public static final String PARAM_EXTRA_TRELLO_CARD = "com.mili.xiaominglui.app.vello.extra.trello.card";
    public static final String PARAM_EXTRA_FORCE_GET_OPEN_TRELLO_CARD = "com.mili.xiaominglui.app.vello.extra.force.get.open.trello.card";

    private VelloRequestFactory() {
        // no public constructor
    }

    public static Request getOpenTrelloCardListRequest(int startId, boolean force) {
        Request request = new Request(REQUEST_TYPE_GET_OPEN_TRELLO_CARD_LIST);
        request.setMemoryCacheEnabled(true);
        request.put(PARAM_EXTRA_SERVICE_START_ID, startId);
        request.put(PARAM_EXTRA_FORCE_GET_OPEN_TRELLO_CARD, force);
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
    
    public static Request lookUpInDictionaryRequest(String keyword) {
    	Request request = new Request(REQUEST_TYPE_LOOK_UP_IN_DICTIONARY);
    	request.put(PARAM_EXTRA_QUERY_WORD_KEYWORD, keyword);
    	request.setMemoryCacheEnabled(true);
    	return request;
    }

    public static Request queryInRemoteStorageRequest(String query, String jsonResponse) {
        Request request = new Request(REQUEST_TYPE_QUERY_IN_REMOTE_STORAGE);
        request.put(PARAM_EXTRA_QUERY_WORD_KEYWORD, query);
        request.put(PARAM_EXTRA_DICTIONARY_WS_RESULT, jsonResponse);
        request.setMemoryCacheEnabled(true);
        return request;
    }

    public static Request addWordCardRequest(String keyword, String wsResult) {
        Request request = new Request(REQUEST_TYPE_ADD_WORDCARD);
        request.put(PARAM_EXTRA_QUERY_WORD_KEYWORD, keyword);
        request.put(PARAM_EXTRA_DICTIONARY_WS_RESULT, wsResult);
        request.setMemoryCacheEnabled(true);
        return request;
    }

    public static Request reStartWordCardRequest(String idCard) {
        Request request = new Request(REQUEST_TYPE_RESTART_WORDCARD);
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
    
	public static Request archiveWordCardRequest(String idCard) {
		Request request = new Request(REQUEST_TYPE_ARCHIVE_WORDCARD);
		request.put(PARAM_EXTRA_VOCABULARY_CARD_ID, idCard);
		request.setMemoryCacheEnabled(true);
		return request;
	}

	public static Request createWebHook() {
		Request request = new Request(REQUEST_TYPE_CREATE_WEBHOOK);
		request.setMemoryCacheEnabled(true);
		return request;
	}
	
	public static Request deleteWebHook() {
		Request request = new Request(REQUEST_TYPE_DELETE_WEBHOOK);
		request.setMemoryCacheEnabled(false);
		return request;
	}

	public static Request checkTrelloConnection() {
		Request request = new Request(REQUEST_TYPE_CHECK_TRELLO_CONNECTION);
		request.setMemoryCacheEnabled(false);
		return request;
	}
	
	public static Request readTrelloAccountUserName(String accessToken) {
		Request request = new Request(REQUEST_TYPE_READ_TRELLO_ACCOUNT_USERNAME);
		request.put(PARAM_EXTRA_TRELLO_ACCESS_TOKEN, accessToken);
		request.setMemoryCacheEnabled(true);
		return request;
	}
	
	public static Request deleteRemoteTrelloCard(TrelloCard card, int startId) {
		Request request = new Request(REQUEST_TYPE_DELETE_REMOTE_TRELLO_CARD);
		request.put(PARAM_EXTRA_TRELLO_CARD, card);
		request.put(PARAM_EXTRA_SERVICE_START_ID, startId);
		request.setMemoryCacheEnabled(true);
		return request;
	}
	
	public static Request updateRemoteTrelloCard(TrelloCard card, int startId) {
		Request request = new Request(REQUEST_TYPE_UPDATE_REMOTE_TRELLO_CARD);
		request.put(PARAM_EXTRA_TRELLO_CARD, card);
		request.put(PARAM_EXTRA_SERVICE_START_ID, startId);
		request.setMemoryCacheEnabled(true);
		return request;
	}
}
