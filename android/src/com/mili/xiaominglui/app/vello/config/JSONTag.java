package com.mili.xiaominglui.app.vello.config;

public final class JSONTag {
	private JSONTag() {
		// No public constructor
	}

	// Trello Board tags
	public static final String BOARD_ELEM_ID = "id";
	public static final String BOARD_ELEM_NAME = "name";
	public static final String BOARD_ELEM_DESC = "desc";
	public static final String BOARD_ELEM_CLOSED = "closed";
	public static final String BOARD_ELEM_IDORGANIZATION = "idOrganization";

	// Trello List tags
	public static final String LIST_ELEM_ID = "id";
	public static final String LIST_ELEM_NAME = "name";
	public static final String LIST_ELEM_CLOSED = "closed";

	// Trello Card tags
	public static final String CARD_ELEM_ID = "id";
	public static final String CARD_ELEM_NAME = "name";
	public static final String CARD_ELEM_DUE = "due";
	public static final String CARD_ELEM_DESC = "desc";
	public static final String CARD_ELEM_IDLIST = "idList";
	public static final String CARD_ELEM_CLOSED = "closed";
	public static final String CARD_ELEM_DATELASTACTIVITY = "dateLastActivity";

	// Trello Search tags
	public static final String SEARCH_ELEM_CARDS = "cards";
	
	// Trello Model tags
	public static final String MODEL_ELEM_MODEL = "model";
	public static final String MODEL_ELEM_ID = "id";
	public static final String MODEL_ELEM_DESC = "desc";
	public static final String MODEL_ELEM_NAME = "name";
	// Trello Action tags
	public static final String ACTION_ELEM_ACTION = "action";
	public static final String ACTION_ELEM_ID = "id";
	public static final String ACTION_ELEM_DATA = "data";
	public static final String ACTION_ELEM_DATA_OLD = "old";
	public static final String ACTION_ELEM_TYPE = "type";
	public static final String ACTION_ELEM_DATA_CARD = "card";
	public static final String ACTION_ELEM_DATA_CARD_NAME = "name";
	
	// AVOS tags
	public static final String AVOS_INFORMATION = "infomation";

	// Mili Dictionary tags
	public static final String MILI_DICTIONARY_SPELL = "spell";
	public static final String MILI_DICTIONARY_PRON = "pron";
	public static final String MILI_DICTIONARY_PRON_TYPE = "type";
	public static final String MILI_DICTIONARY_PRON_PS = "ps";
	public static final String MILI_DICTIONARY_PRON_LINK = "link";
	public static final String MILI_DICTIONARY_ACCETTATION = "accettation";
	public static final String MILI_DICTIONARY_ACCETTATION_POS = "pos";
	public static final String MILI_DICTIONARY_ACCETTATION_ACCEP = "accep";

}
