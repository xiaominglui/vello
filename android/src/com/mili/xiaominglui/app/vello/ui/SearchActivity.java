package com.mili.xiaominglui.app.vello.ui;

import java.lang.ref.WeakReference;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.devspark.appmsg.AppMsg;
import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.data.model.WordCard;
import com.mili.xiaominglui.app.vello.data.provider.MySuggestionProvider;
import com.mili.xiaominglui.app.vello.service.VelloService;
import com.mili.xiaominglui.app.vello.util.AccountUtils;
import com.mili.xiaominglui.app.vello.util.ReflectionUtils;
import com.mili.xiaominglui.app.vello.util.UIUtils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.SearchRecentSuggestions;
import android.text.Html;
import android.view.Gravity;
import android.widget.SearchView;
import android.widget.Toast;


public class SearchActivity extends BaseActivity {
	private static final String TAG = SearchActivity.class.getSimpleName();
	private Context mContext;
	private Activity mActivity;
	
	private SearchActivityUIHandler mUICallback = new SearchActivityUIHandler(this);

	static class SearchActivityUIHandler extends Handler {
		WeakReference<SearchActivity> mActivity;

		SearchActivityUIHandler(SearchActivity activity) {
			mActivity = new WeakReference<SearchActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			SearchActivity theActivity = mActivity.get();
			switch (msg.what) {
			case VelloService.MSG_DIALOG_BAD_DATA_ERROR_SHOW:
				theActivity.showBadDataErrorDialog();
				break;
			case VelloService.MSG_DIALOG_CONNECTION_ERROR_SHOW:
				// TODO
				// ConnectionErrorDialogFragment.show();
				break;
			case VelloService.MSG_TOAST_INIT_VOCABULARY_START:
				AppMsg.makeText(theActivity.mActivity,
						R.string.toast_init_vocabulary_start, AppMsg.STYLE_INFO)
						.setLayoutGravity(Gravity.BOTTOM).show();
				break;
			case VelloService.MSG_TOAST_GET_DUE_WORD:
				AppMsg.makeText(theActivity.mActivity,
						R.string.toast_get_due_word, AppMsg.STYLE_INFO)
						.setLayoutGravity(Gravity.BOTTOM).show();
				break;
			case VelloService.MSG_TOAST_INIT_VOCABULARY_END:
				AppMsg.makeText(theActivity.mActivity,
						R.string.toast_init_vocabulary_end, AppMsg.STYLE_INFO)
						.setLayoutGravity(Gravity.BOTTOM).show();
				break;
			case VelloService.MSG_TOAST_NO_WORD_NOW:
				AppMsg.makeText(theActivity.mActivity,
						R.string.toast_no_word_now, AppMsg.STYLE_CONFIRM)
						.setLayoutGravity(Gravity.TOP).show();
				break;
			case VelloService.MSG_TOAST_NOT_AVAILABLE_WORD:
				AppMsg.makeText(theActivity.mActivity,
						R.string.toast_not_available_word, AppMsg.STYLE_ALERT)
						.setLayoutGravity(Gravity.TOP).show();
				break;
			case VelloService.MSG_SHOW_RESULT_WORDCARD:
			    WordCard result = (WordCard) msg.obj;
			    Toast.makeText(theActivity, result.id, Toast.LENGTH_SHORT).show();
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
			// Tell the user about this for our demo.
			Toast.makeText(getApplicationContext(),
					R.string.local_service_connected, Toast.LENGTH_SHORT)
					.show();

			try {
				Message msg = Message.obtain(null,
						VelloService.MSG_REGISTER_CLIENT);
				msg.replyTo = mMessenger;
				mService.send(msg);
			} catch (RemoteException e) {
				// In this case the service has crashed before we could even do
				// anything with it
			}

			if (AccountUtils.hasVocabularyBoard(mContext)
					&& AccountUtils.isVocabularyBoardWellFormed(mContext)) {
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
			Toast.makeText(getApplicationContext(),
					R.string.local_service_disconnected, Toast.LENGTH_SHORT)
					.show();
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
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mContext = getApplicationContext();
		mActivity = this;
//		setContentView(R.layout.activity_search);
		
		Intent intent  = getIntent();

	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	        String query = intent.getStringExtra(SearchManager.QUERY);
	        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
	                MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
	        suggestions.saveRecentQuery(query, null);
	    }
	    
	    doBindService();
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
        String query = intent.getStringExtra(SearchManager.QUERY);

        setTitle(Html.fromHtml(getString(R.string.title_search_query, query)));
        
        doWordSearch(query);
	}
	
	private void doWordSearch(String query) {
		// TODO
		// 1. check if the word is in local cache, show if yes, go on if no
		// 2. check if the word is in remote but closed, show if yes and re-open, go on if no
		// 3. query dictionary service
		// 4. show and insert local cache item
		// 5. sync for ensuring adding to remote successfully
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
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
        getSupportMenuInflater().inflate(R.menu.search, menu);
        setupSearchMenuItem(menu);
        return true;
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupSearchMenuItem(Menu menu) {
        final MenuItem searchItem = menu.findItem(R.id.menu_search);
        if (searchItem != null && UIUtils.hasHoneycomb()) {
            SearchView searchView = (SearchView) searchItem.getActionView();
            if (searchView != null) {
                SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
                searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        ReflectionUtils.tryInvoke(searchItem, "collapseActionView");
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        return false;
                    }
                });
                searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
                    @Override
                    public boolean onSuggestionSelect(int i) {
                        return false;
                    }

                    @Override
                    public boolean onSuggestionClick(int i) {
                        ReflectionUtils.tryInvoke(searchItem, "collapseActionView");
                        return false;
                    }
                });
            }
        }
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_search:
			if (!UIUtils.hasHoneycomb()) {
				startSearch(null, false, Bundle.EMPTY, false);
				return true;
			}
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}
