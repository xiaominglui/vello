package com.mili.xiaominglui.app.vello.receiver;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.PushService;
import com.mili.xiaominglui.app.vello.authenticator.Constants;
import com.mili.xiaominglui.app.vello.config.JSONTag;
import com.mili.xiaominglui.app.vello.config.VelloConfig;
import com.mili.xiaominglui.app.vello.config.WSConfig;
import com.mili.xiaominglui.app.vello.service.VelloService;
import com.mili.xiaominglui.app.vello.syncadapter.SyncHelper;
import com.mili.xiaominglui.app.vello.ui.MainActivity;
import com.mili.xiaominglui.app.vello.ui.SettingsActivity;
import com.mili.xiaominglui.app.vello.util.AccountUtils;

public class AVOSPushReceiver extends BroadcastReceiver {
	private static final String TAG = AVOSPushReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "onReceive --- " + action);
		}
		
		if (action.equals("android.intent.action.BOOT_COMPLETED")) {
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
			int syncFreqValue = Integer.valueOf(settings.getString(SettingsActivity.KEY_PREF_SYNC_FREQ, "24"));
			boolean monitor = settings.getBoolean(SettingsActivity.KEY_PREF_DICT_CLIPBOARD_MONITOR, false);
			if (monitor) {
				Intent startMonitor = new Intent(context, VelloService.class);
				startMonitor.putExtra("monitor", true);
				ComponentName service = context.startService(startMonitor);
	            if (service == null) {
	                Log.e(TAG, "Can't start service " + VelloService.class.getName());
	            }
			}

			if (syncFreqValue == 0) {
				// save Installation for push
				PushService.setDefaultPushCallback(context, MainActivity.class);
				AVInstallation.getCurrentInstallation().saveInBackground();
			}
		}

		if (action.equals("com.avos.avello.SYNC_MSG")) {
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
					JSONObject jsonActionData = jsonAction.getJSONObject(JSONTag.ACTION_ELEM_DATA);

					if (actionType.equals(WSConfig.WS_TRELLO_ACTION_TYPE_CREATECARD)) {
						// createCard action
						// query and insert a card

						JSONObject jsonActionDataCard = jsonActionData.getJSONObject(JSONTag.ACTION_ELEM_DATA_CARD);
						String cardName = jsonActionDataCard.getString(JSONTag.ACTION_ELEM_DATA_CARD_NAME);
						
						if (VelloConfig.DEBUG_SWITCH) {
							Log.d(TAG, "action create --- name=" + cardName);
						}

						SyncHelper.requestManualSync(new Account(AccountUtils.getChosenAccountName(context), Constants.ACCOUNT_TYPE));

					} else if (actionType.equals(WSConfig.WS_TRELLO_ACTION_TYPE_UPDATECARD)) {
						// updateCard action
						// distinguish sub-type
						JSONObject jsonActionDataOld = jsonActionData.getJSONObject(JSONTag.ACTION_ELEM_DATA_OLD);
						JSONObject jsonActionDataCard = jsonActionData.getJSONObject(JSONTag.ACTION_ELEM_DATA_CARD);
						String cardName = jsonActionDataCard.getString(JSONTag.ACTION_ELEM_DATA_CARD_NAME);
						if (jsonActionDataOld.has(JSONTag.CARD_ELEM_IDLIST)) {
							// sub-type: move list
							if (VelloConfig.DEBUG_SWITCH) {
								Log.d(TAG, "action recalled --- name=" + cardName);
							}
							SyncHelper.requestManualSync(new Account(AccountUtils.getChosenAccountName(context), Constants.ACCOUNT_TYPE));
						} else if (jsonActionDataOld.has(JSONTag.CARD_ELEM_DUE)) {
							// sub-type: change due
						} else if (jsonActionDataOld.has(JSONTag.CARD_ELEM_CLOSED)) {
							// sub-type: change closed
							if (VelloConfig.DEBUG_SWITCH) {
								Log.d(TAG, "action closed --- name=" + cardName);
							}
							SyncHelper.requestManualSync(new Account(AccountUtils.getChosenAccountName(context), Constants.ACCOUNT_TYPE));
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
		}
	}
}
