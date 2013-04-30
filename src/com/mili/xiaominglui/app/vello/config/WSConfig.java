package com.mili.xiaominglui.app.vello.config;

public final class WSConfig {
    private WSConfig() {
	// No public constructor
    }

    public static final String ROOT_URL = "https://api.trello.com";
    public static final String API_VERSION = "/1";
    public static final String TRELLO_API_URL = ROOT_URL + API_VERSION;
    
    public static final String GET_MY_BOARD_LIST = "/members/me/boards";
    
    public static final String VELLO_APP_KEY = "d8b28623f3171a9dbc870738cd5f6926";
    
    public static final String WS_TRELLO_PARAM_FILTER = "filter";
    public static final String WS_TRELLO_PARAM_APP_KEY = "key";
    public static final String WS_TRELLO_PARAM_ACCESS_TOKEN = "token";
    public static final String WS_TRELLO_PARAM_FIELDS = "fields";
    
}
