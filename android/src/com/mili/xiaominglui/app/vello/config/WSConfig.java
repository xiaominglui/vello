package com.mili.xiaominglui.app.vello.config;

public final class WSConfig {
    private WSConfig() {
	// No public constructor
    }

    public static final String ROOT_URL = "https://api.trello.com";
    public static final String API_VERSION = "/1";
    public static final String TRELLO_API_URL = ROOT_URL + API_VERSION;
    
    public static final String VELLO_APP_KEY = "d8b28623f3171a9dbc870738cd5f6926";
    
    public static final String GET_MY_BOARD_LIST = "/members/me/boards";
    
    public static final String WS_TRELLO_TARGET_BOARD = "/boards";
    public static final String WS_TRELLO_TARGET_LIST = "/lists";
    public static final String WS_TRELLO_TARGET_SEARCH = "/search";
    public static final String WS_TRELLO_TARGET_CARD ="/cards";
    public static final String WS_TRELLO_TARGET_TOKEN = "/tokens";
    public static final String WS_TRELLO_TARGET_WEBHOOKS = "/webhooks";
    public static final String WS_TRELLO_TARGET_MEMBERS = "/members";
    
    public static final String WS_TRELLO_PARAM_FILTER = "filter";
    public static final String WS_TRELLO_PARAM_APP_KEY = "key";
    public static final String WS_TRELLO_PARAM_ACCESS_TOKEN = "token";
    public static final String WS_TRELLO_PARAM_FIELDS = "fields";
    public static final String WS_TRELLO_PARAM_CARDS = "cards";
    public static final String WS_TRELLO_PARAM_CLOSED = "closed";
    public static final String WS_TRELLO_PARAM_NAME = "name";
    public static final String WS_TRELLO_PARAM_DESC = "desc";
    public static final String WS_TRELLO_PARAM_IDBOARD = "idBoard";
    public static final String WS_TRELLO_PARAM_IDBOARDS = "idBoards";
    public static final String WS_TRELLO_PARAM_QUERY = "query";
    public static final String WS_TRELLO_PARAM_MODELTYPES = "modelTypes";
    public static final String WS_TRELLO_PARAM_CARD_FIELDS = "card_fields";
    public static final String WS_TRELLO_PARAM_IDLIST = "idList";
    public static final String WS_TRELLO_PARAM_DUE = "due";
    public static final String WS_TRELLO_PARAM_TEXT = "text";
    public static final String WS_TRELLO_PARAM_WEBHOOK_DESCRIPTION = "description";
    public static final String WS_TRELLO_PARAM_WEBHOOK_CALLBACK_URL = "callbackURL";
    public static final String WS_TRELLO_PARAM_WEBHOOK_ID_MODEL = "idModel";
    public static final String WS_TRELLO_PARAM_WEBHOOK_ACTIVE = "active";
    
    
    
    public static final String WS_TRELLO_FIELD_LISTS = "/lists";
    public static final String WS_TRELLO_FIELD_CARDS = "/cards";
    public static final String WS_TRELLO_FIELD_LIST = "/list";
    public static final String WS_TRELLO_FIELD_USERNAME = "/username";
    
    public static final String WS_TRELLO_ACTION_COMMENTS = "/actions/comments";
    
    public static final String WS_TRELLO_ACTION_TYPE_UPDATECARD = "updateCard";
    public static final String WS_TRELLO_ACTION_TYPE_CREATECARD = "createCard";
    
    // iciba
    public static final String WS_DICTIONARY_ICIBA_API = "http://dict-co.iciba.com/api/dictionary.php";
    public static final String WS_DICTIONARY_ICIBA_PARAM_KEYWORD = "w";
    
    // mili dictionary
    public static final String WS_DICTIONARY_MILI_API = "http://mili.cmlife.com.cn:8090/words/";
    
    // avos cloud
    public static final String WS_AVELLO_TRELISTENER_URL = "http://dev.vaa.avosapps.com/trelistener";
}
