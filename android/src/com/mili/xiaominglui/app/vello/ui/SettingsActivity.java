package com.mili.xiaominglui.app.vello.ui;

import java.lang.ref.WeakReference;

import android.accounts.Account;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.authenticator.Constants;
import com.mili.xiaominglui.app.vello.config.VelloConfig;
import com.mili.xiaominglui.app.vello.data.provider.VelloProvider;
import com.mili.xiaominglui.app.vello.service.VelloService;
import com.mili.xiaominglui.app.vello.util.NetworkUtil;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	public static final String KEY_PREF_SYNC_FREQ = "pref_sync_frequency";
	public static final String KEY_PREF_SYNC_WIFI_ONLY = "pref_sync_wifi_only";
	
	private String mNewSyncValue = "0";
	private ListPreference mListPreference;
	private SettingsActivityUIHandler mUICallback = new SettingsActivityUIHandler(this);
	private class SettingsActivityUIHandler extends Handler {
		WeakReference<SettingsActivity> mActivity;
		
		SettingsActivityUIHandler(SettingsActivity activity) {
			mActivity = new WeakReference<SettingsActivity>(activity);
		}
		
		@Override
		public void handleMessage(Message msg) {
			SettingsActivity theActivity = mActivity.get();
			switch (msg.what) {
			case VelloService.MSG_STATUS_WEBHOOK_ACTIVED:
				theActivity.postActiveWebhook();
				break;
			case VelloService.MSG_STATUS_WEBHOOK_DEACTIVED:
				theActivity.postDeactiveWebhook();
				break;
			case VelloService.MSG_VALID_TRELLO_CONNECTION:
				theActivity.withValidTrelloConnection();
				break;
			case VelloService.MSG_INVALID_TRELLO_CONNECTION:
				theActivity.withInvalidTrelloConnection();
				break;
			}
		}
	}
	Messenger mService = null;
	private boolean mIsBound;

	final Messenger mMessenger = new Messenger(mUICallback);

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service. Because we have bound to a explicit
			// service that we know is running in our own process, we can
			// cast its IBinder to a concrete class and directly access it.
			mService = new Messenger(service);

			try {
				Message msg = Message.obtain(null,
						VelloService.MSG_REGISTER_CLIENT);
				msg.replyTo = mMessenger;
				mService.send(msg);
			} catch (RemoteException e) {
				// In this case the service has crashed before we could even do
				// anything with it
			}
			
			sendMessageToService(VelloService.MSG_CHECK_TRELLO_CONNECTION, null);
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected - process crashed.
			mService = null;
		}
	};

	private void sendMessageToService(int type, Object obj) {
		if (mIsBound) {
			if (mService != null) {
				try {
					Message msg = Message.obtain(null, type);
					if (obj != null) {
						msg.obj = obj;
					}
					msg.replyTo = mMessenger;
					mService.send(msg);
				} catch (RemoteException e) {
				}
			}
		}
	}
	
	void doBindService() {
		// Establish a connection with the service. We use an explicit
		// class name because we want a specific service implementation that
		// we know will be running in our own process (and thus won't be
		// supporting component replacement by other applications).
		bindService(new Intent(this, VelloService.class), mConnection,
				Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	void doUnbindService() {
		if (mIsBound) {
			// Detach our existing connection.
			unbindService(mConnection);
			mIsBound = false;
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
		
		doBindService();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// setup the initial value
		mListPreference.setEnabled(false);
		mListPreference.setSummary(R.string.pref_sync_frequency_summary_retrieving_setting);
		if (NetworkUtil.isOnline(getApplicationContext())) {
			
		} else {
			mListPreference.setSummary(R.string.pref_sync_frequency_summary_no_network);
		}
		
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		setupSimplePreferencesScreen();
		mListPreference = (ListPreference) getPreferenceScreen().findPreference(KEY_PREF_SYNC_FREQ);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		doUnbindService();
	}
	
	private void setupSimplePreferencesScreen() {
        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.preferences);
    }

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(KEY_PREF_SYNC_FREQ)) {
			ListPreference syncFreqPref = (ListPreference) findPreference(key);
			syncFreqPref.setSummary(syncFreqPref.getEntry());
			mNewSyncValue = syncFreqPref.getValue();
			syncFreqPref.setEnabled(false);
			if (mNewSyncValue.equals("0")) {
				// choose PUSH
				// active PUSH
				sendMessageToService(VelloService.MSG_SET_WEBHOOK_ACTIVE_STATUS, true);
			} else {
				// choose schedule sync
				// deactive PUSH
				sendMessageToService(VelloService.MSG_SET_WEBHOOK_ACTIVE_STATUS, false);
			}
		}
	}
	
	private void postActiveWebhook() {
		Account account = new Account(VelloConfig.TRELLO_DEFAULT_ACCOUNT_NAME, Constants.ACCOUNT_TYPE);
		Bundle extras = new Bundle();
		ContentResolver.removePeriodicSync(account, VelloProvider.AUTHORITY, extras);
		mListPreference.setEnabled(true);
	}
	
	private void postDeactiveWebhook() {
		Account account = new Account(VelloConfig.TRELLO_DEFAULT_ACCOUNT_NAME, Constants.ACCOUNT_TYPE);
		Bundle extras = new Bundle();
		int pollFrequency = Integer.valueOf(mNewSyncValue) * 60 * 60;
		ContentResolver.addPeriodicSync(account, VelloProvider.AUTHORITY, extras, pollFrequency);
		mListPreference.setEnabled(true);
	}
	
	private void withValidTrelloConnection() {
		mListPreference.setEnabled(true);
		mListPreference.setSummary(mListPreference.getEntry());
	}
	
	private void withInvalidTrelloConnection() {
		mListPreference.setSummary(R.string.pref_sync_frequency_summary_no_reliable_network);
		mListPreference.setEnabled(false);
	}
}
