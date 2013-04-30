package com.mili.xiaominglui.app.vello.data.service;

import com.foxykeep.datadroid.service.RequestService;
import com.mili.xiaominglui.app.vello.data.operation.TrelloBoardListOperation;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;

public class VelloRequestService extends RequestService {
    
    @Override
    protected int getMaximumNumberOfThreads() {
        return 1;
    }

    @Override
    public Operation getOperationForType(int requestType) {
	switch (requestType) {
	case VelloRequestFactory.REQUEST_TYPE_QUERY_TRELLO_BOARD:
	    return new TrelloBoardListOperation();
	case VelloRequestFactory.REQUEST_TYPE_QUERY_WORD_LIST_DOING:
	    return null;
	case VelloRequestFactory.REQUEST_TYPE_QUERY_WORD:
	    return null;
	}
	return null;
    }

}
