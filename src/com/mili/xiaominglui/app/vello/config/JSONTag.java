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
    
    // Trello Search tags
    public static final String SEARCH_ELEM_CARDS = "cards";

}
