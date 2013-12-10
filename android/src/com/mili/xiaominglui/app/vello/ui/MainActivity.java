package com.mili.xiaominglui.app.vello.ui;

import java.lang.ref.WeakReference;

import android.accounts.Account;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.avos.avoscloud.AVAnalytics;
import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.authenticator.Constants;
import com.mili.xiaominglui.app.vello.config.VelloConfig;
import com.mili.xiaominglui.app.vello.data.model.TrelloCard;
import com.mili.xiaominglui.app.vello.data.provider.VelloProvider;
import com.mili.xiaominglui.app.vello.service.VelloService;
import com.mili.xiaominglui.app.vello.syncadapter.SyncHelper;
import com.mili.xiaominglui.app.vello.util.AccountUtils;
import com.mili.xiaominglui.app.vello.util.HelpUtils;

public class MainActivity extends BaseActivity implements HomeViewFragment.onStatusChangedListener {
	private static final String TAG = MainActivity.class.getSimpleName();
	
	private static final int CONTENT_VIEW_ID = 666;
	private Drawable oldBackground = null;
	private int currentColor = 0xFF666666;
	private final Handler handler = new Handler();
	private Fragment mHomeViewFragment;
	
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
			case VelloService.MSG_DIALOG_CONNECTION_ERROR_SHOW:
				// TODO
//				 ConnectionErrorDialogFragment.show();
				break;
			case VelloService.MSG_TOAST_INIT_VOCABULARY_START:
//				superActivityToast = new SuperActivityToast(theActivity.getApplicationContext(), SuperToast.Type.STANDARD);
//				AppMsg.makeText(theActivity.mActivity,
//						R.string.toast_init_vocabulary_start, AppMsg.STYLE_INFO)
//						.setLayoutGravity(Gravity.BOTTOM).show();
				break;
			case VelloService.MSG_TOAST_INIT_VOCABULARY_END:
				break;
			case VelloService.MSG_TOAST_NO_WORD_NOW:
//				AppMsg.makeText(theActivity.mActivity,
//						R.string.toast_no_word_now, AppMsg.STYLE_CONFIRM)
//						.setLayoutGravity(Gravity.TOP).show();
				break;
			case VelloService.MSG_TOAST_NOT_AVAILABLE_WORD:
//				AppMsg.makeText(theActivity.mActivity,
//						R.string.toast_not_available_word, AppMsg.STYLE_ALERT)
//						.setLayoutGravity(Gravity.TOP).show();
				break;
			case VelloService.MSG_TOAST_WORD_REVIEWED_COUNT_PLUS:
				break;
			case VelloService.MSG_SHOW_RESULT_WORDCARD:
			    TrelloCard result = (TrelloCard) msg.obj;
			    Toast.makeText(theActivity, result.id, Toast.LENGTH_SHORT).show();
			    break;
			case VelloService.MSG_AUTH_TOKEN_REVOKED:
				theActivity.postAuthTokenRevoked();
				break;
			case VelloService.MSG_STATUS_INIT_ACCOUNT_BEGIN:
				theActivity.preInitAccount();
				break;
			case VelloService.MSG_STATUS_INIT_ACCOUNT_END:
				theActivity.postInitAccount();
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

			if (AccountUtils.hasVocabularyBoard(getApplicationContext()) &&
					AccountUtils.isVocabularyBoardWellFormed(getApplicationContext())) {
				// all initialized
				// do nothing now
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

        if (isFinishing()) {
            return;
        }

        FrameLayout frame = new FrameLayout(this);
        frame.setId(CONTENT_VIEW_ID);
        setContentView(frame, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        
        if (savedInstanceState == null) {
			setInitialFragment();
		}

		handleIntent(getIntent());
		doBindService();
		
		AVAnalytics.trackAppOpened(getIntent());
	}
	
	private void setInitialFragment() {
		mHomeViewFragment = HomeViewFragment.newInstance();
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.add(CONTENT_VIEW_ID, mHomeViewFragment).commit();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		doUnbindService();
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
					Message msg = Message.obtain(null,
							VelloService.MSG_TRIGGER_QUERY_WORD);
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
		getSupportMenuInflater().inflate(R.menu.home, menu);

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
			break; **/
		case R.id.menu_sync:
			triggerRefresh();
			return true;
			
		case R.id.menu_about:
			HelpUtils.showAbout(this);
			return true;
			
		case R.id.menu_settings:
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
            
		case R.id.menu_sign_out:
			sendMessageToService(VelloService.MSG_REVOKE_AUTH_TOKEN);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void triggerRefresh() {
		SyncHelper.requestManualSync(new Account(AccountUtils.getChosenAccountName(this), Constants.ACCOUNT_TYPE));
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
		FragmentManager fm = getSupportFragmentManager();
		fm.beginTransaction().replace(CONTENT_VIEW_ID, new ProgressFragment()).commit();
	}
	
	private void postInitAccount() {
		Account account = new Account(VelloConfig.TRELLO_DEFAULT_ACCOUNT_NAME, Constants.ACCOUNT_TYPE);
		ContentResolver.setIsSyncable(account, VelloProvider.AUTHORITY, 1);
		ContentResolver.setSyncAutomatically(account, VelloProvider.AUTHORITY, true);
		FragmentManager fm = getSupportFragmentManager();
		fm.beginTransaction().replace(CONTENT_VIEW_ID, mHomeViewFragment).commit();
	}
	
	private void postAuthTokenRevoked() {
		AccountUtils.signOut(this);
		finish();
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
	public void onAllReviewed() {
	}

	@Override
	public void onWordReviewed() {
//		reviewedCountPlusToastShow();
	}
}
