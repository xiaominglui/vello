package com.mili.xiaominglui.app.vello.receiver;

import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

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
				JSONObject json = new JSONObject(intent.getExtras().getString(
						"com.parse.Data"));
				Iterator itr = json.keys();
				while (itr.hasNext()) {
					String key = (String) itr.next();
					Log.d(TAG, "..." + key + " => " + json.getString(key));
				}
			} catch (JSONException e) {
				Log.d(TAG, "JSONException: " + e.getMessage());
			}
			Toast.makeText(context, "SYNC_MSG", Toast.LENGTH_SHORT).show();
		}
	}
}
