package com.mili.xiaominglui.app.vello.data.service;

import com.foxykeep.datadroid.service.RequestService;
import com.mili.xiaominglui.app.vello.data.operation.CheckVocabularyBoardOperation;
import com.mili.xiaominglui.app.vello.data.operation.CheckVocabularyListOperation;
import com.mili.xiaominglui.app.vello.data.operation.ConfigureVocabularyBoardOperation;
import com.mili.xiaominglui.app.vello.data.operation.CreateVocabularyBoardOperation;
import com.mili.xiaominglui.app.vello.data.operation.CreateVocabularyListOperation;
import com.mili.xiaominglui.app.vello.data.operation.ReopenVocabularyListOperation;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;

public class VelloRequestService extends RequestService {
    
    @Override
    protected int getMaximumNumberOfThreads() {
        return 1;
    }

    @Override
    public Operation getOperationForType(int requestType) {
	switch (requestType) {
	case VelloRequestFactory.REQUEST_TYPE_CHECK_VOCABULARY_BOARD:
	    return new CheckVocabularyBoardOperation();
	case VelloRequestFactory.REQUEST_TYPE_CONFIGURE_VOCABULARY_BOARD:
	    return new ConfigureVocabularyBoardOperation();
	case VelloRequestFactory.REQUEST_TYPE_CHECK_VOCABULARY_LIST:
	    return new CheckVocabularyListOperation();
	case VelloRequestFactory.REQUEST_TYPE_REOPEN_VOCABULARY_LIST:
	    return new ReopenVocabularyListOperation();
	case VelloRequestFactory.REQUEST_TYPE_CREATE_VOCABULARY_LIST:
	    return new CreateVocabularyListOperation();
	case VelloRequestFactory.REQUEST_TYPE_CREATE_VOCABULARY_BOARD:
	    return new CreateVocabularyBoardOperation();
	case VelloRequestFactory.REQUEST_TYPE_QUERY_WORD_LIST_DOING:
	    return null;
	case VelloRequestFactory.REQUEST_TYPE_QUERY_WORD:
	    return null;
	}
	return null;
    }

}
