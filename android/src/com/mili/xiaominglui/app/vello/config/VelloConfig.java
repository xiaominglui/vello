package com.mili.xiaominglui.app.vello.config;

public class VelloConfig {
    public static final boolean DEBUG_SWITCH = true;

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
    
    public static int DICTIONARY_MODE_ACTION_BAR_COLOR = 0xFF3F9FE0;
    public static int REVIEW_MODE_ACTION_BAR_COLOR = 0xFF222222;
    
    // ACRA related
    public static final String CRASH_LOGGER_CARD_ID_SHORTLINK = "https://trello.com/c/pamv10IM";

}
