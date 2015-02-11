
package com.mili.xiaominglui.app.vello.data.operation;

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
import com.mili.xiaominglui.app.vello.data.factory.InitializeWordCardResponseJsonFactory;
import com.mili.xiaominglui.app.vello.data.factory.ReviewedWordCardResponseJsonFactory;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;
import com.mili.xiaominglui.app.vello.util.AccountUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

public class ReviewedWordCardOperation implements Operation {
    private static final String TAG = ReviewedWordCardOperation.class
            .getSimpleName();

    @Override
    public Bundle execute(Context context, Request request)
            throws ConnectionException, DataException, CustomRequestException {
        String token = AccountUtils.getAuthToken(context);
        String idCard = request
                .getString(VelloRequestFactory.PARAM_EXTRA_VOCABULARY_CARD_ID);
        int position = request
                .getInt(VelloRequestFactory.PARAM_EXTRA_VOCABULARY_LIST_POSITION);

        String urlString = WSConfig.TRELLO_API_URL
                + WSConfig.WS_TRELLO_TARGET_CARD + "/" + idCard;
        HashMap<String, String> parameterMap = new HashMap<String, String>();

        if (position == VelloConfig.VOCABULARY_LIST_POSITION_8TH) {
            // should archive the wordcard
            parameterMap.put(WSConfig.WS_TRELLO_PARAM_CLOSED, "true");
            if (VelloConfig.DEBUG_SWITCH) {
                Log.d(TAG, "last position, word card is closed");
            }

        } else {
            Calendar rightNow = Calendar.getInstance();
            long rightNowUnixTime = rightNow.getTimeInMillis();
            long rightNowUnixTimeGMT = rightNowUnixTime - TimeZone.getDefault().getRawOffset();
            long delta = VelloConfig.VOCABULARY_LIST_DUE_DELTA[position];
            long dueUnixTime = rightNowUnixTimeGMT + delta;

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            Date dueDate = new Date(dueUnixTime);
            String stringDueDate = format.format(dueDate);
            String newIdList = AccountUtils.getVocabularyListId(context,
                    position + 1);
            parameterMap.put(WSConfig.WS_TRELLO_PARAM_DUE, stringDueDate);
            parameterMap.put(WSConfig.WS_TRELLO_PARAM_IDLIST, newIdList);

            if (VelloConfig.DEBUG_SWITCH) {
                Log.d(TAG, "stringDueDate = " + stringDueDate);
                Log.d(TAG, "newIdList = " + newIdList);
            }
        }

        parameterMap.put(WSConfig.WS_TRELLO_PARAM_APP_KEY,
                WSConfig.VELLO_APP_KEY);
        parameterMap.put(WSConfig.WS_TRELLO_PARAM_ACCESS_TOKEN, token);

        NetworkConnection networkConnection = new NetworkConnection(context,
                urlString);
        networkConnection.setMethod(Method.PUT);
        networkConnection.setParameters(parameterMap);
        ConnectionResult result = networkConnection.execute();

        if (VelloConfig.DEBUG_SWITCH) {
            Log.d(TAG, "result.body = " + result.body);
        }
        return ReviewedWordCardResponseJsonFactory.parseResult(result.body);
    }

}
