package com.mili.xiaominglui.app.vello.config;

public class VelloConfig {

    // Vocabulary Lists Config
    public static final int VOCABULARY_LIST_POSITION_NEW = 0;
    public static final int VOCABULARY_LIST_POSITION_1ST = 1;
    public static final int VOCABULARY_LIST_POSITION_2ND = 2;
    public static final int VOCABULARY_LIST_POSITION_3RD = 3;
    public static final int VOCABULARY_LIST_POSITION_4TH = 4;
    public static final int VOCABULARY_LIST_POSITION_5TH = 5;
    public static final int VOCABULARY_LIST_POSITION_6TH = 6;
    public static final int VOCABULARY_LIST_POSITION_7TH = 7;
    public static final int VOCABULARY_LIST_POSITION_8TH = 8;

    public static final long VOCABULARY_LIST_DUE_DELTA[] = {
	5 * 60 * 1000,
	30 * 60 * 1000,
	12 * 60 * 60 * 1000,
	1 * 24 * 60 * 60 * 1000,
	2 * 24 * 60 * 60 * 1000,
	4 * 24 * 60 * 60 * 1000,
	7 * 24 * 60 * 60 * 1000,
	15 * 24 * 60 * 60 * 1000,
	-1 };
    public static final long RELEARNED_DUE_DELTA = 15 * 60 * 1000;
    public static final boolean DEBUG_SWITCH = true;

    public static int DICTIONARY_MODE_ACTION_BAR_COLOR = 0xFF3F9FE0;
    public static int REVIEW_MODE_ACTION_BAR_COLOR = 0xFF222222;
    
    public static final String TRELLO_DEFAULT_ACCOUNT_NAME = "me";
    
    // ACRA related
    public static final String CRASH_LOGGER_CARD_ID_SHORTLINK = "https://trello.com/c/pamv10IM";

    // Trello Web Hook related
    public static final String DEFAULT_VOCABULARY_BOARD_HOOK_DESCRIPTION = "Vocabulary Sync Trigger";
    
	public static final String[] DRAWER_MENU_TITLES = { "Reviewing", "Mastered"};
	
	public static final String API_KEY = "d8b28623f3171a9dbc870738cd5f6926";
	public static final String API_SECRET = "71ae43e7beebe76bd75bd98cc81425e04979dfc3cb2a12f16094e15f5e348fcf";

	public static final int FLOAT_DICT_CARD_DISMISS_TIME = 5 * 1000;

    public static final String PRE_SHOW_INTRO = "show_intro";

    public static final String SAMPLE_BOARD_ID = "4ff6eeafd0b1db1e63b17afd";
    
}
