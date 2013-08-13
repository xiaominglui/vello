
package com.mili.xiaominglui.app.vello.data.factory;

import android.os.Bundle;
import android.util.Log;

import com.foxykeep.datadroid.exception.DataException;
import com.mili.xiaominglui.app.vello.config.JSONTag;
import com.mili.xiaominglui.app.vello.config.VelloConfig;
import com.mili.xiaominglui.app.vello.data.model.WordCard;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DueWordCardListJsonFactory {
    private static final String TAG = DueWordCardListJsonFactory.class
            .getSimpleName();

    private DueWordCardListJsonFactory() {
        // No public constructor
    }

    public static Bundle parseResult(String wsResponse) throws DataException {
        ArrayList<WordCard> wordCardList = new ArrayList<WordCard>();
        Calendar rightNow = Calendar.getInstance();
        long rightNowUnixTime = rightNow.getTimeInMillis();
        long rightNowUnixTimeGMT = rightNowUnixTime - TimeZone.getDefault().getRawOffset();

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
                    if (dueUnixTime <= rightNowUnixTimeGMT) {
                        // it is time to review
                        WordCard wordCard = new WordCard();
                        wordCard.id = jsonCard.getString(JSONTag.CARD_ELEM_ID);
                        wordCard.name = jsonCard
                                .getString(JSONTag.CARD_ELEM_NAME);
                        wordCard.desc = jsonCard
                                .getString(JSONTag.BOARD_ELEM_DESC);
                        wordCard.due = jsonCard
                                .getString(JSONTag.CARD_ELEM_DUE);
                        wordCard.idList = jsonCard
                                .getString(JSONTag.CARD_ELEM_IDLIST);
                        wordCardList.add(wordCard);
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSONException", e);
            throw new DataException(e);
        } catch (ParseException e) {
            Log.e(TAG, "ParseException", e);
        }

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(
                VelloRequestFactory.BUNDLE_EXTRA_WORDCARD_LIST, wordCardList);
        return bundle;
    }

}
