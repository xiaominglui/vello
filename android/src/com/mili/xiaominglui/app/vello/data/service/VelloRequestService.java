
package com.mili.xiaominglui.app.vello.data.service;

import com.foxykeep.datadroid.service.RequestService;
import com.mili.xiaominglui.app.vello.data.operation.CheckTrelloConnectionOperation;
import com.mili.xiaominglui.app.vello.data.operation.DeleteRemoteTrelloCardOperation;
import com.mili.xiaominglui.app.vello.data.operation.ReadTrelloAccountInfoOperation;
import com.mili.xiaominglui.app.vello.data.operation.deleteWebHookOperation;
import com.mili.xiaominglui.app.vello.data.operation.AddWordCardOperation;
import com.mili.xiaominglui.app.vello.data.operation.ArchiveWordCardOperation;
import com.mili.xiaominglui.app.vello.data.operation.CheckVocabularyBoardOperation;
import com.mili.xiaominglui.app.vello.data.operation.CheckVocabularyListOperation;
import com.mili.xiaominglui.app.vello.data.operation.CreateWebHookOperation;
import com.mili.xiaominglui.app.vello.data.operation.QueryInRemoteStorageOperation;
import com.mili.xiaominglui.app.vello.data.operation.ConfigureVocabularyBoardOperation;
import com.mili.xiaominglui.app.vello.data.operation.CreateVocabularyBoardOperation;
import com.mili.xiaominglui.app.vello.data.operation.CreateVocabularyListOperation;
import com.mili.xiaominglui.app.vello.data.operation.GetOpenTrelloCardListOperation;
import com.mili.xiaominglui.app.vello.data.operation.InitializeWordCardOperation;
import com.mili.xiaominglui.app.vello.data.operation.LookUpInDictionaryOperation;
import com.mili.xiaominglui.app.vello.data.operation.ReopenVocabularyListOperation;
import com.mili.xiaominglui.app.vello.data.operation.RestartWordCardOperation;
import com.mili.xiaominglui.app.vello.data.operation.ReviewedWordCardOperation;
import com.mili.xiaominglui.app.vello.data.operation.UpdateRemoteTrelloCardOperation;
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
            case VelloRequestFactory.REQUEST_TYPE_GET_OPEN_TRELLO_CARD_LIST:
            	return new GetOpenTrelloCardListOperation();
            case VelloRequestFactory.REQUEST_TYPE_QUERY_IN_REMOTE_STORAGE:
                return new QueryInRemoteStorageOperation();
            case VelloRequestFactory.REQUEST_TYPE_ADD_WORDCARD:
                return new AddWordCardOperation();
            case VelloRequestFactory.REQUEST_TYPE_RESTART_WORDCARD:
                return new RestartWordCardOperation();
            case VelloRequestFactory.REQUEST_TYPE_INITIALIZE_WORDCARD:
                return new InitializeWordCardOperation();
            case VelloRequestFactory.REQUEST_TYPE_REVIEWED_WORDCARD:
                return new ReviewedWordCardOperation();
            case VelloRequestFactory.REQUEST_TYPE_ARCHIVE_WORDCARD:
            	return new ArchiveWordCardOperation();
            case VelloRequestFactory.REQUEST_TYPE_UPGRADE_WORDCARD:
            	return new UpgradeWordCardOperation();
            case VelloRequestFactory.REQUEST_TYPE_LOOK_UP_IN_DICTIONARY:
            	return new LookUpInDictionaryOperation();
            case VelloRequestFactory.REQUEST_TYPE_CREATE_WEBHOOK:
            	return new CreateWebHookOperation();
            case VelloRequestFactory.REQUEST_TYPE_DELETE_WEBHOOK:
            	return new deleteWebHookOperation();
            case VelloRequestFactory.REQUEST_TYPE_CHECK_TRELLO_CONNECTION:
            	return new CheckTrelloConnectionOperation();
            case VelloRequestFactory.REQUEST_TYPE_READ_TRELLO_ACCOUNT_INFO:
            	return new ReadTrelloAccountInfoOperation();
            case VelloRequestFactory.REQUEST_TYPE_DELETE_REMOTE_TRELLO_CARD:
            	return new DeleteRemoteTrelloCardOperation();
            case VelloRequestFactory.REQUEST_TYPE_UPDATE_REMOTE_TRELLO_CARD:
            	return new UpdateRemoteTrelloCardOperation();
        }
        return null;
    }

}
