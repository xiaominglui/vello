package com.mili.xiaominglui.app.vello.ui;

import java.lang.ref.WeakReference;


import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.base.C;
import com.mili.xiaominglui.app.vello.base.log.L;
import com.mili.xiaominglui.app.vello.config.VelloConfig;
import com.mili.xiaominglui.app.vello.data.provider.VelloProvider;
import com.mili.xiaominglui.app.vello.fragment.ReviewViewFragment;
import com.mili.xiaominglui.app.vello.service.VelloService;
import com.mili.xiaominglui.app.vello.util.AccountUtils;
import com.mili.xiaominglui.app.vello.util.CommonUtils;
import com.mili.xiaominglui.app.vello.util.HelpUtils;

public class MainActivity extends BaseActivity implements ConnectionTimeOutFragment.ConnectionTimeOutFragmentEventListener {
	private static final String TAG = MainActivity.class.getSimpleName();
	
	private boolean isInFront;

	private MainActivityUIHandler mUICallback = new MainActivityUIHandler(this);

	static class MainActivityUIHandler extends Handler {
		WeakReference<MainActivity> mActivity;

		MainActivityUIHandler(MainActivity activity) {
			mActivity = new WeakReference<>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			MainActivity theActivity = mActivity.get();
			switch (msg.what) {
			case VelloService.MSG_DIALOG_BAD_DATA_ERROR_SHOW:
				theActivity.showBadDataErrorDialog();
				break;
			case VelloService.MSG_STATUS_INIT_ACCOUNT_END:
                L.d(TAG, "status_init_account_end");
				theActivity.postInitAccount();
                break;
			case VelloService.MSG_STATUS_SYNC_END:
				theActivity.postSync();
				break;
			case VelloService.MSG_STATUS_REVOKE_BEGIN:
				theActivity.preAuthTokenRevoke();
				break;
			case VelloService.MSG_STATUS_CONNECTION_TIMEOUT:
				if (theActivity.isInFront) {
					theActivity.showConnectionTimeoutView();
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

			if (!AccountUtils.hasVocabularyBoard(C.get()) || !AccountUtils.isVocabularyBoardWellFormed(C.get())) {
				sendMessageToService(VelloService.MSG_CHECK_VOCABULARY_BOARD);
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected - process crashed.
			mService = null;
		}
	};

	private void sendMessageToService(int type) {
		if (mIsBound) {
			if (mService != null) {
				try {
					Message msg = Message.obtain(null, type);
					msg.replyTo = mMessenger;
					mService.send(msg);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	}

	void doBindService() {
		// Establish a connection with the service. We use an explicit
		// class name because we want a specific service implementation that
		// we know will be running in our own process (and thus won't be
		// supporting component replacement by other applications).
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

        if (isFinishing()) {
            return;
        }
        setContentView(R.layout.activity_main);

        FragmentManager fm = getFragmentManager();
		fm.beginTransaction().add(R.id.fragment_container_master, ReviewViewFragment.newInstance()).commit();

		doBindService();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		isInFront = true;
		super.onResume();
	}

	@Override
	protected void onPause() {
		isInFront = false;
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
        doUnbindService();
		super.onDestroy();
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		onNewIntent(getIntent());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		/** disable search at present
		case R.id.menu_search:
			if (!UIUtils.hasHoneycomb()) {
                startSearch(null, false, Bundle.EMPTY, false);
                return true;
            }
			break; 
		case R.id.menu_sync:
			return true;**/

		case R.id.menu_about:
			HelpUtils.showAbout(this);
			return true;

		case R.id.menu_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;

		case R.id.menu_sign_out:
			localSignOut();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void localSignOut() {
		AccountUtils.signOut(this);
		// restart
		Intent intent = getIntent();
		finish();
		startActivity(intent);
	}

	private void postInitAccount() {
        L.d(TAG, "postInitAccount()");
		ContentResolver.setIsSyncable(AccountUtils.getAccount(getApplicationContext()), VelloProvider.AUTHORITY, 1);
		ContentResolver.setSyncAutomatically(AccountUtils.getAccount(getApplicationContext()), VelloProvider.AUTHORITY, true);

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		int syncFreqValue = Integer.valueOf(settings.getString(SettingsActivity.KEY_PREF_SYNC_FREQ, "24"));
		if (syncFreqValue > 0) {
			Bundle extras = new Bundle();
			ContentResolver.addPeriodicSync(AccountUtils.getAccount(getApplicationContext()), VelloProvider.AUTHORITY, extras, syncFreqValue * 60 * 60);
		}

        CommonUtils.triggerRefresh();
	}
	private void postSync() {
        L.d(TAG, "postSync");
        ReviewViewFragment reviewViewFragment = (ReviewViewFragment) getFragmentManager().findFragmentById(R.id.fragment_container_master);
        reviewViewFragment.synced();
	}

	private void preAuthTokenRevoke() {
		FragmentManager fm = getFragmentManager();
		fm.beginTransaction().replace(R.id.fragment_container_master, new ProgressFragment()).commit();
	}
	
	private void showConnectionTimeoutView() {
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "showConnectionTimeoutView called, with isInFront=" + isInFront);
		}
		if (isInFront) {
			FragmentManager fm = getFragmentManager();
			fm.beginTransaction().replace(R.id.fragment_container_master, ConnectionTimeOutFragment.newInstance()).commit();
		}
	}
	
	@Override
	public void onReload() {
	}
}
