package com.mili.xiaominglui.app.vello.data.factory;

import android.os.Bundle;
import android.util.Log;

import com.foxykeep.datadroid.exception.DataException;
import com.mili.xiaominglui.app.vello.config.JSONTag;
import com.mili.xiaominglui.app.vello.data.model.List;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class VocabularyListJsonFactory {
    private static final String TAG = VocabularyListJsonFactory.class
	    .getSimpleName();

    private VocabularyListJsonFactory() {
	// No public constructor
    }

    public static Bundle parseResult(String wsResponse) throws DataException {
	ArrayList<List> listList = new ArrayList<List>();
	try {
	    JSONArray jsonListArray = new JSONArray(wsResponse);
	    int size = jsonListArray.length();
	    for (int i = 0; i < size; i++) {
		JSONObject jsonList = jsonListArray.getJSONObject(i);
		List list = new List();
		list.id = jsonList.getString(JSONTag.LIST_ELEM_ID);
		list.name = jsonList.getString(JSONTag.LIST_ELEM_NAME);
		list.closed = jsonList.getString(JSONTag.LIST_ELEM_CLOSED);

		listList.add(list);
	    }

	} catch (JSONException e) {
	    Log.e(TAG, "JSONException", e);
	    throw new DataException(e);
	}
	Bundle bundle = new Bundle();
	bundle.putParcelableArrayList(VelloRequestFactory.BUNDLE_EXTRA_VOCABULARY_LIST_LIST, listList);
	return bundle;
    }
}
