package com.mili.xiaominglui.app.vello.ui;

import java.lang.ref.WeakReference;


import android.accounts.Account;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
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
import com.mili.xiaominglui.app.vello.authenticator.Constants;
import com.mili.xiaominglui.app.vello.base.log.L;
import com.mili.xiaominglui.app.vello.config.VelloConfig;
import com.mili.xiaominglui.app.vello.data.model.TrelloCard;
import com.mili.xiaominglui.app.vello.data.provider.VelloProvider;
import com.mili.xiaominglui.app.vello.fragment.ReviewViewFragment;
import com.mili.xiaominglui.app.vello.service.VelloService;
import com.mili.xiaominglui.app.vello.syncadapter.SyncHelper;
import com.mili.xiaominglui.app.vello.util.AccountUtils;
import com.mili.xiaominglui.app.vello.util.HelpUtils;

public class MainActivity extends BaseActivity implements ReviewViewFragment.onStatusChangedListener, ConnectionTimeOutFragment.ConnectionTimeOutFragmentEventListener {
	private static final String TAG = MainActivity.class.getSimpleName();
	
	private Drawable oldBackground = null;
	private int currentColor = 0xFF666666;
	private boolean isInFront;
	private final Handler handler = new Handler();
	private NotificationManager mNM;
	
	private MainActivityUIHandler mUICallback = new MainActivityUIHandler(this);

	static class MainActivityUIHandler extends Handler {
		WeakReference<MainActivity> mActivity;

		MainActivityUIHandler(MainActivity activity) {
			mActivity = new WeakReference<MainActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			MainActivity theActivity = mActivity.get();
			switch (msg.what) {
			case VelloService.MSG_DIALOG_BAD_DATA_ERROR_SHOW:
				theActivity.showBadDataErrorDialog();
				break;
			case VelloService.MSG_SHOW_RESULT_WORDCARD:
			    TrelloCard result = (TrelloCard) msg.obj;
			    break;
			case VelloService.MSG_STATUS_INIT_ACCOUNT_BEGIN:
				if (theActivity.isInFront) {
					theActivity.preInitAccount();
				}
				break;
			case VelloService.MSG_STATUS_INIT_ACCOUNT_END:
                L.d(TAG, "status_init_account_end");
				theActivity.postInitAccount();
                break;
			case VelloService.MSG_STATUS_SYNC_BEGIN:
				theActivity.preSync();
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

			if (AccountUtils.hasVocabularyBoard(getApplicationContext()) && AccountUtils.isVocabularyBoardWellFormed(getApplicationContext())) {
				// all initialized
				// if has no open card, blank page after init
				// if has open card, review page
			} else {
				// begin to check vocabulary board
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
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        setContentView(R.layout.activity_main);

        FragmentManager fm = getFragmentManager();
		fm.beginTransaction().add(R.id.fragment_container_master, ReviewViewFragment.newInstance()).commit();

		handleIntent(getIntent());
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
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
	}
	
	private void handleIntent(Intent intent) {

	}
	
	private void doWordSearch(String query) {
		// TODO
		// 1. check if the word is in remote but closed, show if yes and re-open, go on if no
		// 2. query dictionary service
		// 3. show and insert local cache item
		// 4. sync for ensuring adding to remote successfully
		if (mIsBound) {
			if (mService != null) {
				try {
					Message msg = Message.obtain(null, VelloService.MSG_TRIGGER_QUERY_WORD);
					msg.obj = query;
					msg.replyTo = mMessenger;
					mService.send(msg);
				} catch (RemoteException e) {
				}
			}
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.home, menu);

//		setupSearchMenuItem(menu);

		return true;
	}
	/* hide search function
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupSearchMenuItem(Menu menu) {
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        if (searchItem != null && UIUtils.hasHoneycomb()) {
            SearchView searchView = (SearchView) searchItem.getActionView();
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
				
				@Override
				public boolean onQueryTextSubmit(String query) {
				    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
					inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
					return true;
				}
				
				@Override
				public boolean onQueryTextChange(String newText) {
					if (mHomeViewFragment.isAdded()) {
						((HomeViewFragment) mHomeViewFragment).onQueryTextChange(newText);
					}
					
					return true;
				}
			});
            if (searchView != null) {
                SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
                searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            }
        }
    }
    */

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
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
			int syncFreqValue = Integer.valueOf(settings.getString(SettingsActivity.KEY_PREF_SYNC_FREQ, "24"));
			if (syncFreqValue == 0) {
			}
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

	private void triggerRefresh() {
        L.d(TAG, "triggerRefresh");
		SyncHelper.requestManualSync(new Account(AccountUtils.getAccountName(this), Constants.ACCOUNT_TYPE));
	}
	
	private void changeColor(int newColor) {
		// change ActionBar color just if an ActionBar is available
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

			Drawable colorDrawable = new ColorDrawable(newColor);
			Drawable bottomDrawable = getResources().getDrawable(R.drawable.actionbar_bottom);
			LayerDrawable ld = new LayerDrawable(new Drawable[] { colorDrawable, bottomDrawable });

			if (oldBackground == null) {

				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
					ld.setCallback(drawableCallback);
				} else {
					getActionBar().setBackgroundDrawable(ld);
				}

			} else {

				TransitionDrawable td = new TransitionDrawable(new Drawable[] { oldBackground, ld });

				// workaround for broken ActionBarContainer drawable handling on
				// pre-API 17 builds
				// https://github.com/android/platform_frameworks_base/commit/a7cc06d82e45918c37429a59b14545c6a57db4e4
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
					td.setCallback(drawableCallback);
				} else {
					getActionBar().setBackgroundDrawable(td);
				}

				td.startTransition(200);

			}

			oldBackground = ld;

			// http://stackoverflow.com/questions/11002691/actionbar-setbackgrounddrawable-nulling-background-from-thread-handler
			getActionBar().setDisplayShowTitleEnabled(false);
			getActionBar().setDisplayShowTitleEnabled(true);

		}

		currentColor = newColor;

	}
	private void preInitAccount() {
		FragmentManager fm = getFragmentManager();
		fm.beginTransaction().replace(R.id.fragment_container_master, new ProgressFragment()).commit();
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

        triggerRefresh(); // keep this avoid not auto sync after account inited
	}
	
	private void preSync() {
        L.d(TAG, "preSync");
		if (isInFront) {
			FragmentManager fm = getFragmentManager();
			fm.beginTransaction().replace(R.id.fragment_container_master, new ProgressFragment()).commit();
		}
	}
	
	private void postSync() {
        L.d(TAG, "postSync");
        if (isInFront) {
            FragmentManager fm = getFragmentManager();
            fm.beginTransaction().replace(R.id.fragment_container_master, ReviewViewFragment.newInstance()).commit();
        }
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
	
	private Drawable.Callback drawableCallback = new Drawable.Callback() {
		@Override
		public void invalidateDrawable(Drawable who) {
			getActionBar().setBackgroundDrawable(who);
		}

		@Override
		public void scheduleDrawable(Drawable who, Runnable what, long when) {
			handler.postAtTime(what, when);
		}

		@Override
		public void unscheduleDrawable(Drawable who, Runnable what) {
			handler.removeCallbacks(what);
		}
	};

	@Override
	public void onModeChanged(int modeColor) {
		changeColor(modeColor);
	}

	@Override
	public void syncOnAllRecalled() {
	}

	@Override
	public void onWordRecalled() {
	}

	@Override
	public void onReload() {
	}
}
