
package com.mili.xiaominglui.app.vello.util;

import android.content.Context;
import android.util.Log;

import com.foxykeep.datadroid.exception.ConnectionException;
import com.foxykeep.datadroid.network.NetworkConnection;
import com.foxykeep.datadroid.network.NetworkConnection.ConnectionResult;
import com.foxykeep.datadroid.network.NetworkConnection.Method;
import com.mili.xiaominglui.app.vello.config.VelloConfig;
import com.mili.xiaominglui.app.vello.config.WSConfig;
import com.mili.xiaominglui.app.vello.data.factory.AddWordCardResponseJsonFactory;

import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;

import java.util.HashMap;

public class ACRATrelloSender implements ReportSender {
    private static final String TAG = ACRATrelloSender.class.getSimpleName();
    private Context mContext;

    public ACRATrelloSender(Context context) {
        mContext = context;
    }

    @Override
    public void send(CrashReportData data) throws ReportSenderException {
        String token = AccountUtils.getAuthToken(mContext);
        String urlString = WSConfig.TRELLO_API_URL + WSConfig.WS_TRELLO_TARGET_CARD
                + "/5188ff6a127b7449510127cb" + WSConfig.WS_TRELLO_ACTION_COMMENTS;

        String dataString = data.toString();
        int len = dataString.length();
        if (VelloConfig.DEBUG_SWITCH) {
            Log.d(TAG, "dat length = " + String.valueOf(len));
            Log.d(TAG, "dataString = " + dataString);
        }
        HashMap<String, String> parameterMap = new HashMap<String, String>();
        parameterMap.put(WSConfig.WS_TRELLO_PARAM_TEXT, dataString);
        parameterMap.put(WSConfig.WS_TRELLO_PARAM_APP_KEY, WSConfig.VELLO_APP_KEY);
        parameterMap.put(WSConfig.WS_TRELLO_PARAM_ACCESS_TOKEN, token);

        NetworkConnection networkConnection = new NetworkConnection(mContext,
                urlString);
        networkConnection.setMethod(Method.POST);
        networkConnection.setParameters(parameterMap);
        ConnectionResult result;
        try {
            result = networkConnection.execute();
            if (VelloConfig.DEBUG_SWITCH) {
                Log.d(TAG, "result.body = " + result.body);
            }
        } catch (ConnectionException e) {
            e.printStackTrace();
        }
    }
}
