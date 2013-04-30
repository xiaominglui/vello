package com.mili.xiaominglui.app.vello.data.requestmanager;

import com.foxykeep.datadroid.requestmanager.Request;
import com.mili.xiaominglui.app.vello.data.operation.TrelloBoardListOperation;
import com.mili.xiaominglui.app.vello.data.operation.WordListOperation;

public final class VelloRequestFactory {
    
    // Request types
    public static final int REQUEST_TYPE_QUERY_WORD_LIST_DOING = 0;
    public static final int REQUEST_TYPE_QUERY_WORD = 1;
    public static final int REQUEST_TYPE_QUERY_TRELLO_BOARD = 3;
    
    // Response data
    public static final String BUNDLE_EXTRA_TRELLO_BOARD_LIST = "com.mili.xiaominglui.app.vello.extra.boardList";
    
    private VelloRequestFactory() {
	// no public constructor
    }
    
    public static Request getMyDoingWordListRequest(int returnFormat) {
	Request request = new Request(REQUEST_TYPE_QUERY_WORD_LIST_DOING);
	request.put(WordListOperation.PARAM_RETURN_FORMAT, returnFormat);
	return request;
    }
    
    public static Request queryTrelloBoardRequest() {
	Request request = new Request(REQUEST_TYPE_QUERY_TRELLO_BOARD);
	request.setMemoryCacheEnabled(true);
	return request;
    }

}
