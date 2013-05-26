
package com.mili.xiaominglui.app.vello.data.operation;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.foxykeep.datadroid.exception.ConnectionException;
import com.foxykeep.datadroid.exception.CustomRequestException;
import com.foxykeep.datadroid.exception.DataException;
import com.foxykeep.datadroid.network.NetworkConnection;
import com.foxykeep.datadroid.network.NetworkConnection.ConnectionResult;
import com.foxykeep.datadroid.network.NetworkConnection.Method;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.service.RequestService.Operation;
import com.mili.xiaominglui.app.vello.config.VelloConfig;
import com.mili.xiaominglui.app.vello.config.WSConfig;
import com.mili.xiaominglui.app.vello.data.factory.AllWordCardListJsonFactory;
import com.mili.xiaominglui.app.vello.data.factory.SyncTrelloDBResponseJsonFactory;
import com.mili.xiaominglui.app.vello.data.model.WordCard;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbWordCard;
import com.mili.xiaominglui.app.vello.util.AccountUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class SyncTrelloDBOperation implements Operation {
    private static final String TAG = SyncTrelloDBOperation.class.getSimpleName();

    @Override
    public Bundle execute(Context context, Request request) throws ConnectionException,
            DataException, CustomRequestException {
        String token = AccountUtils.getAuthToken(context);
        String vocabularyBoardId = AccountUtils.getVocabularyBoardId(context);

        String urlString = WSConfig.TRELLO_API_URL
                + WSConfig.WS_TRELLO_TARGET_BOARD + "/" + vocabularyBoardId
                + WSConfig.WS_TRELLO_FIELD_CARDS;

        HashMap<String, String> parameterMap = new HashMap<String, String>();
        parameterMap.put(WSConfig.WS_TRELLO_PARAM_FILTER, "open");
        parameterMap.put(WSConfig.WS_TRELLO_PARAM_FIELDS, "name,desc,due,idList");
        parameterMap.put(WSConfig.WS_TRELLO_PARAM_APP_KEY, WSConfig.VELLO_APP_KEY);
        parameterMap.put(WSConfig.WS_TRELLO_PARAM_ACCESS_TOKEN, token);

        NetworkConnection networkConnection = new NetworkConnection(context,
                urlString);
        networkConnection.setMethod(Method.GET);
        networkConnection.setParameters(parameterMap);
        ConnectionResult result = networkConnection.execute();

        ArrayList<WordCard> wordCardList = new ArrayList<WordCard>();
        wordCardList = AllWordCardListJsonFactory.parseResult(result.body);
        if (VelloConfig.DEBUG_SWITCH) {
            Log.d(TAG, "result.body = " + result.body);
        }

        // Clear the table
//        context.getContentResolver().delete(DbWordCard.CONTENT_URI, null, null);

        int wordCardListSize = wordCardList.size();
        if (wordCardListSize > 0) {
            ArrayList<ContentProviderOperation> operationList =
                    new ArrayList<ContentProviderOperation>();
            Calendar rightNow = Calendar.getInstance();
            long rightNowUnixTime = rightNow.getTimeInMillis();
            SimpleDateFormat format = new SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            Date date;

            for (WordCard wordCard : wordCardList) {
                String dueString = wordCard.due;
                if (!dueString.equals("null")) {
                    try {
                        date = format.parse(dueString);
                        long dueUnixTime = date.getTime();
                        if (dueUnixTime <= rightNowUnixTime) {
                            // it is time to review, insert words to local DB
                            // cache
                            operationList.add(ContentProviderOperation.newInsert(DbWordCard.CONTENT_URI).withValues(wordCard.toContentVaalues()).build());
                            
                        }
                    } catch (ParseException e) {
                        Log.e(TAG, "ParseException", e);
                    }
                }
            }
        }

        return null;
    }

}
