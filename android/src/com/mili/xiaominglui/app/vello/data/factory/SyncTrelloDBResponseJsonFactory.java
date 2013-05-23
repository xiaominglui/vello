package com.mili.xiaominglui.app.vello.data.factory;

import android.os.Bundle;
import android.util.Log;

import com.foxykeep.datadroid.exception.DataException;
import com.mili.xiaominglui.app.vello.config.JSONTag;
import com.mili.xiaominglui.app.vello.data.model.WordCard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SyncTrelloDBResponseJsonFactory {
    private static final String TAG = SyncTrelloDBResponseJsonFactory.class.getSimpleName();
    
    private SyncTrelloDBResponseJsonFactory() {
        // No public constructor
    }

    public static Bundle parseResult(String wsResponse) throws DataException {
        Calendar rightNow = Calendar.getInstance();
        long rightNowUnixTime = rightNow.getTimeInMillis();
        
        try {
            JSONArray jsonCardArray = new JSONArray(wsResponse);
            int size = jsonCardArray.length();
            for (int i = 0; i < size; i++) {
                JSONObject jsonCard = jsonCardArray.getJSONObject(i);
                String dueString = jsonCard.getString(JSONTag.CARD_ELEM_DUE);
                if (!dueString.equals("null")) {
                    SimpleDateFormat format = new SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

                    Date date = format.parse(dueString);
                    long dueUnixTime = date.getTime();
                    if (dueUnixTime <= rightNowUnixTime) {
                        // it is time to review, insert words to local DB cache
                        // TODO
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException", e);
            throw new DataException(e);
        } catch (ParseException e) {
            Log.e(TAG, "ParseException", e);
        }
        // TODO Auto-generated method stub
        return null;
    }

}
