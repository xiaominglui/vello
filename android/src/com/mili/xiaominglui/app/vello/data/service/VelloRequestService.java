
package com.mili.xiaominglui.app.vello.data.service;

import com.foxykeep.datadroid.service.RequestService;
import com.mili.xiaominglui.app.vello.data.operation.AddWordCardOperation;
import com.mili.xiaominglui.app.vello.data.operation.ArchiveWordCardOperation;
import com.mili.xiaominglui.app.vello.data.operation.CheckVocabularyBoardOperation;
import com.mili.xiaominglui.app.vello.data.operation.CheckVocabularyListOperation;
import com.mili.xiaominglui.app.vello.data.operation.CheckWordCardStatusOperation;
import com.mili.xiaominglui.app.vello.data.operation.ConfigureVocabularyBoardOperation;
import com.mili.xiaominglui.app.vello.data.operation.CreateVocabularyBoardOperation;
import com.mili.xiaominglui.app.vello.data.operation.CreateVocabularyListOperation;
import com.mili.xiaominglui.app.vello.data.operation.GetDueWordCardListOperation;
import com.mili.xiaominglui.app.vello.data.operation.InitializeWordCardOperation;
import com.mili.xiaominglui.app.vello.data.operation.LookUpWordOperation;
import com.mili.xiaominglui.app.vello.data.operation.ReopenVocabularyListOperation;
import com.mili.xiaominglui.app.vello.data.operation.ReopenWordCardOperation;
import com.mili.xiaominglui.app.vello.data.operation.ReviewedPlusWordCardOperation;
import com.mili.xiaominglui.app.vello.data.operation.ReviewedWordCardOperation;
import com.mili.xiaominglui.app.vello.data.operation.SyncLocalCacheOperation;
import com.mili.xiaominglui.app.vello.data.operation.UpgradeWordCardOperation;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;

public class VelloRequestService extends RequestService {

    @Override
    protected int getMaximumNumberOfThreads() {
        return 3;
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
            case VelloRequestFactory.REQUEST_TYPE_GET_DUE_WORDCARD_LIST:
                return new GetDueWordCardListOperation();
            case VelloRequestFactory.REQUEST_TYPE_LOOK_UP_WORD:
                return new LookUpWordOperation();
            case VelloRequestFactory.REQUEST_TYPE_CHECK_WORDCARD_STATUS:
                return new CheckWordCardStatusOperation();
            case VelloRequestFactory.REQUEST_TYPE_ADD_WORDCARD:
                return new AddWordCardOperation();
            case VelloRequestFactory.REQUEST_TYPE_REOPEN_WORDCARD:
                return new ReopenWordCardOperation();
            case VelloRequestFactory.REQUEST_TYPE_INITIALIZE_WORDCARD:
                return new InitializeWordCardOperation();
            case VelloRequestFactory.REQUEST_TYPE_REVIEWED_WORDCARD:
                return new ReviewedWordCardOperation();
            case VelloRequestFactory.REQUEST_TYPE_REVIEWED_PLUS_WORDCARD:
                return new ReviewedPlusWordCardOperation();
            case VelloRequestFactory.REQUEST_TYPE_ARCHIVE_WORDCARD:
            	return new ArchiveWordCardOperation();
            case VelloRequestFactory.REQUEST_TYPE_UPGRADE_WORDCARD:
            	return new UpgradeWordCardOperation();
            case VelloRequestFactory.REQUEST_TYPE_SYNC_LOCAL_CACHE:
            	return new SyncLocalCacheOperation();
        }
        return null;
    }

}
