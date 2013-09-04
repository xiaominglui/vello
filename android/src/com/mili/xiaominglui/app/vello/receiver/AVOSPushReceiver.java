package com.mili.xiaominglui.app.vello.receiver;

import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import com.mili.xiaominglui.app.vello.config.JSONTag;
import com.mili.xiaominglui.app.vello.config.VelloConfig;
import com.mili.xiaominglui.app.vello.config.WSConfig;
import com.mili.xiaominglui.app.vello.util.AccountUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class AVOSPushReceiver extends BroadcastReceiver {
	private static final String TAG = AVOSPushReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		if (action.equals("com.avos.avello.SYNC_MSG")) {
			// String channel =
			// intent.getExtras().getString("com.parse.Channel");
			try {
				JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
				JSONObject jsonModel = json.getJSONObject(JSONTag.MODEL_ELEM_MODEL);
				String modelID = jsonModel.getString(JSONTag.MODEL_ELEM_ID);
				String modelName = jsonModel.getString(JSONTag.MODEL_ELEM_NAME);
				if (modelID.equals(
						AccountUtils.getVocabularyBoardId(context)) && // same id
						modelName.equals(AccountUtils.getVocabularyBoardName(context)) // same name
						) {
					// verified push data
					JSONObject jsonAction = json.getJSONObject(JSONTag.ACTION_ELEM_ACTION);
					String actionType = jsonAction.getString(JSONTag.ACTION_ELEM_TYPE);
					if (actionType.equals(WSConfig.WS_TRELLO_ACTION_TYPE_CREATECARD)) {
						// createCard action
						// TODO query and insert a card
						
					} else if (actionType.equals(WSConfig.WS_TRELLO_ACTION_TYPE_UPDATECARD)) {
						// updateCard action
						// TODO
						
					} else {
						if (VelloConfig.DEBUG_SWITCH) {
							Log.d(TAG, "unknow action type = " + actionType);
						}
					}
					
				}
			} catch (JSONException e) {
				Log.d(TAG, "JSONException: " + e.getMessage());
			}
			Toast.makeText(context, "SYNC_MSG", Toast.LENGTH_SHORT).show();
		}
	}
}
