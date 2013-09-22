package com.mili.xiaominglui.app.vello.receiver;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SyncStateContract.Constants;
import android.util.Log;
import android.widget.Toast;

import com.mili.xiaominglui.app.vello.config.JSONTag;
import com.mili.xiaominglui.app.vello.config.VelloConfig;
import com.mili.xiaominglui.app.vello.config.WSConfig;
import com.mili.xiaominglui.app.vello.data.provider.VelloProvider;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbWordCard;
import com.mili.xiaominglui.app.vello.util.AccountUtils;

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
				JSONObject jsonInformation = json.getJSONObject(JSONTag.AVOS_INFORMATION);
				JSONObject jsonModel = jsonInformation.getJSONObject(JSONTag.MODEL_ELEM_MODEL);
				String modelID = jsonModel.getString(JSONTag.MODEL_ELEM_ID);
				String modelName = jsonModel.getString(JSONTag.MODEL_ELEM_NAME);
				if (modelID.equals(
						AccountUtils.getVocabularyBoardId(context)) && // same id
						modelName.equals(AccountUtils.getVocabularyBoardName(context)) // same name
						) {
					// verified push data
					JSONObject jsonAction = jsonInformation.getJSONObject(JSONTag.ACTION_ELEM_ACTION);
					String actionType = jsonAction.getString(JSONTag.ACTION_ELEM_TYPE);
					Bundle extras = new Bundle();
					if (actionType.equals(WSConfig.WS_TRELLO_ACTION_TYPE_CREATECARD)) {
						// createCard action
						// query and insert a card
						extras.putString(DbWordCard.SYNC_TYPE, "insert_card");
						ContentResolver.requestSync(new Account(AccountUtils.getChosenAccountName(context), Constants.ACCOUNT_TYPE), VelloProvider.AUTHORITY, extras);
						
					} else if (actionType.equals(WSConfig.WS_TRELLO_ACTION_TYPE_UPDATECARD)) {
						// updateCard action
						// distinguish sub-type
						JSONObject jsonActionData = jsonAction.getJSONObject(JSONTag.ACTION_ELEM_DATA);
						JSONObject jsonActionDataOld = jsonActionData.getJSONObject(JSONTag.ACTION_ELEM_DATA_OLD);
						if (jsonActionDataOld.has(JSONTag.CARD_ELEM_IDLIST)) {
							// sub-type: move list
							
							extras.putString(DbWordCard.SYNC_TYPE, "move_list");
							ContentResolver.requestSync(new Account(AccountUtils.getChosenAccountName(context), Constants.ACCOUNT_TYPE), VelloProvider.AUTHORITY, extras);
						} else if (jsonActionDataOld.has(JSONTag.CARD_ELEM_DUE)) {
							// sub-type: change due
							extras.putString(DbWordCard.SYNC_TYPE, "change_due");
							ContentResolver.requestSync(new Account(AccountUtils.getChosenAccountName(context), Constants.ACCOUNT_TYPE), VelloProvider.AUTHORITY, extras);
						} else if (jsonActionDataOld.has(JSONTag.CARD_ELEM_CLOSED)) {
							// sub-type: change closed
							extras.putString(DbWordCard.SYNC_TYPE, "change_closed");
							ContentResolver.requestSync(new Account(AccountUtils.getChosenAccountName(context), Constants.ACCOUNT_TYPE), VelloProvider.AUTHORITY, extras);
						} else {
							Log.i(TAG, "unknow sub-type of UPDATECARD, data = " + jsonActionData.toString());
						}
						
						
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
