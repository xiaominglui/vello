package com.mili.xiaominglui.app.vello.ui;

import java.lang.ref.WeakReference;

import android.accounts.Account;
import android.app.ActionBar;
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
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.authenticator.Constants;
import com.mili.xiaominglui.app.vello.base.log.L;
import com.mili.xiaominglui.app.vello.data.provider.VelloProvider;
import com.mili.xiaominglui.app.vello.service.VelloService;
import com.mili.xiaominglui.app.vello.util.AccountUtils;
import com.mili.xiaominglui.app.vello.util.NetworkUtil;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    private static final String TAG = SettingsActivity.class.getSimpleName();
    public static final String KEY_PREF_SYNC_FREQ = "pref_sync_frequency";
    public static final String KEY_PREF_SYNC_WIFI_ONLY = "pref_sync_wifi_only";
    public static final String KEY_PREF_DICT_CLIPBOARD_MONITOR = "pref_dict_clipboard_monitor";
    private boolean isInFront;
    private ListPreference mListPreference;
    private SettingsActivityUIHandler mUICallback = new SettingsActivityUIHandler(this);

    static class SettingsActivityUIHandler extends Handler {
        WeakReference<SettingsActivity> mActivity;

        SettingsActivityUIHandler(SettingsActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            SettingsActivity theActivity = mActivity.get();
            switch (msg.what) {
                case VelloService.MSG_VALID_TRELLO_CONNECTION:
                    if (theActivity.isInFront) {
                        theActivity.withValidTrelloConnection();
                    }
                    break;
                case VelloService.MSG_INVALID_TRELLO_CONNECTION:
                    if (theActivity.isInFront) {
                        theActivity.withInvalidTrelloConnection();
                    }
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
                Message msg = Message.obtain(null, VelloService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do
                // anything with it
            }

//            sendMessageToService(VelloService.MSG_CHECK_TRELLO_CONNECTION, null);
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
                    e.printStackTrace();
                }
            }
        }
    }

    void doBindService() {
        bindService(new Intent(this, VelloService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, VelloService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }
            }
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            ActionBar bar = getActionBar();
            if (bar != null)
                getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        isInFront = true;
        super.onResume();
        mListPreference.setEnabled(false);
        mListPreference.setSummary(R.string.pref_sync_frequency_summary_retrieving_setting);

        if (NetworkUtil.isOnline(getApplicationContext())) {
            doBindService();
        } else {
            mListPreference.setSummary(R.string.pref_sync_frequency_summary_no_network);
        }

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        withValidTrelloConnection();
    }

    @Override
    protected void onPause() {
        isInFront = false;
        super.onPause();
        doUnbindService();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setupSimplePreferencesScreen();
        mListPreference = (ListPreference) getPreferenceScreen().findPreference(KEY_PREF_SYNC_FREQ);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("deprecation")
    private void setupSimplePreferencesScreen() {
        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.preferences);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_PREF_SYNC_FREQ)) {
            ListPreference syncFreqPref = (ListPreference) findPreference(key);
            syncFreqPref.setSummary(syncFreqPref.getEntry());
            String mNewSyncValue = syncFreqPref.getValue();
            Account account = new Account(AccountUtils.getAccountName(getApplicationContext()), Constants.ACCOUNT_TYPE);
            Bundle extras = new Bundle();
            int pollFrequency = Integer.valueOf(mNewSyncValue) * 60 * 60;
            ContentResolver.addPeriodicSync(account, VelloProvider.AUTHORITY, extras, pollFrequency);
        } else if (key.equals(KEY_PREF_DICT_CLIPBOARD_MONITOR)) {
            CheckBoxPreference dictClipMonitorPref = (CheckBoxPreference) findPreference(key);
            if (dictClipMonitorPref.isChecked()) {
                Intent startMonitor = new Intent(getApplicationContext(), VelloService.class);
                startMonitor.putExtra("monitor", true);
                ComponentName service = getApplicationContext().startService(startMonitor);
                if (service == null) {
                    L.e(TAG, "Can't start service " + VelloService.class.getName());
                }
            } else {
                sendMessageToService(VelloService.MSG_SHUTDOWN_CLIPBOARD_MONITOR, null);
            }

        }
    }

    private void withValidTrelloConnection() {
        if (isInFront) {
            mListPreference.setEnabled(true);
            mListPreference.setSummary(mListPreference.getEntry());
        }
    }

    private void withInvalidTrelloConnection() {
        if (isInFront) {
            mListPreference.setSummary(R.string.pref_sync_frequency_summary_no_reliable_network);
            mListPreference.setEnabled(false);
        }
    }
}