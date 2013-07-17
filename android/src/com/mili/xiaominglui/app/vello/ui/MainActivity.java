package com.mili.xiaominglui.app.vello.ui;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

import android.accounts.Account;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.database.CharArrayBuffer;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.android.deskclock.widget.ActionableToastBar;
import com.android.deskclock.widget.swipeablelistview.SwipeableListView;
import com.atermenji.android.iconictextview.IconicTextView;
import com.atermenji.android.iconictextview.icon.FontAwesomeIcon;
import com.devspark.appmsg.AppMsg;
import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.authenticator.Constants;
import com.mili.xiaominglui.app.vello.config.VelloConfig;
import com.mili.xiaominglui.app.vello.data.factory.IcibaWordXmlParser;
import com.mili.xiaominglui.app.vello.data.model.Definition;
import com.mili.xiaominglui.app.vello.data.model.Definitions;
import com.mili.xiaominglui.app.vello.data.model.IcibaWord;
import com.mili.xiaominglui.app.vello.data.model.Phonetics;
import com.mili.xiaominglui.app.vello.data.model.Phoneticss;
import com.mili.xiaominglui.app.vello.data.model.WordCard;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbWordCard;
import com.mili.xiaominglui.app.vello.data.provider.VelloProvider;
import com.mili.xiaominglui.app.vello.data.provider.util.ProviderCriteria;
import com.mili.xiaominglui.app.vello.service.VelloService;
import com.mili.xiaominglui.app.vello.util.AccountUtils;

public class MainActivity extends BaseActivity implements
		LoaderCallbacks<Cursor> {
	private static final String TAG = MainActivity.class.getSimpleName();
	private Context mContext;
	private Activity mActivity;

	private static final String KEY_EXPANDED_IDS = "expandedIds";
	private static final String KEY_SELECTED_WORDS = "selectedWords";
	private static final String KEY_DELETED_WORD = "deletedWord";
	private static final String KEY_UNDO_SHOWING = "undoShowing";

	private MyHandler mUICallback = new MyHandler(this);

	static class MyHandler extends Handler {
		WeakReference<MainActivity> mActivity;

		MyHandler(MainActivity activity) {
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

	// Saved status for undo
	private WordCard mDeletedWord;
	private boolean mUndoShowing = false;
	private SwipeableListView mWordsList;
	private WordCardAdapter mAdapter;
	private ActionableToastBar mUndoBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getApplicationContext();
		mActivity = this;

		if (isFinishing()) {
			return;
		}

		handleIntent(getIntent());

		initialize(savedInstanceState);

		getSupportLoaderManager().initLoader(0, null, this);
		doBindService();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			Toast.makeText(mContext, query, Toast.LENGTH_SHORT).show();
			doWordSearch(query);
		}
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

	private void initialize(Bundle savedState) {
		setContentView(R.layout.activity_main);
		int[] expandedIds = null;
		int[] selectedWords = null;

		if (savedState != null) {
			expandedIds = savedState.getIntArray(KEY_EXPANDED_IDS);
			mDeletedWord = savedState.getParcelable(KEY_DELETED_WORD);
			mUndoShowing = savedState.getBoolean(KEY_UNDO_SHOWING);
			selectedWords = savedState.getIntArray(KEY_SELECTED_WORDS);
		}

		mWordsList = (SwipeableListView) findViewById(R.id.words_list);
		mAdapter = new WordCardAdapter(mContext, expandedIds, selectedWords,
				mWordsList);
		mWordsList.setAdapter(mAdapter);
		mWordsList.setVerticalScrollBarEnabled(true);
		mWordsList.enableSwipe(true);
		mWordsList.setOnCreateContextMenuListener(this);
		mWordsList
				.setOnItemSwipeListener(new SwipeableListView.OnItemSwipeListener() {

					@Override
					public void onSwipe(View view) {
						final WordCardAdapter.ItemHolder itemHolder = (WordCardAdapter.ItemHolder) view
								.getTag();
						mAdapter.removeSelectedId(itemHolder.wordcard.idInLocalDB);
						if (!mAdapter.isWordExpanded(itemHolder.wordcard)) {
							asyncMarkDeleteWord(itemHolder.wordcard);
						} else {
							// review failed
							asyncDeleteWordCache(itemHolder.wordcard);
						}
					}
				});

		mWordsList.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				hideUndoBar(true, event);
				return false;
			}
		});
		
		mWordsList.setEmptyView(findViewById(R.id.empty));

		mUndoBar = (ActionableToastBar) findViewById(R.id.undo_bar);

		if (mUndoShowing) {
			mUndoBar.show(new ActionableToastBar.ActionClickedListener() {
				@Override
				public void onActionClicked() {
					asyncUnmarkDeleteWord(mDeletedWord);
					mDeletedWord = null;
					mUndoShowing = false;
				}
			}, 0, getResources().getString(R.string.word_reviewed), true,
					R.string.word_reviewed_undo, true);
		}
	}

	private void asyncDeleteWordCache(WordCard wordcard) {
		Uri uri = ContentUris.withAppendedId(DbWordCard.CONTENT_URI,
				wordcard.idInLocalDB);
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "uri---" + uri.toString() + " is to be deleted");
		}
		getContentResolver().delete(uri, null, null);
	}

	private void asyncUnmarkDeleteWord(final WordCard wordcard) {
		ContentValues cv = new ContentValues();
		cv.put(DbWordCard.Columns.CLOSED.getName(), wordcard.closed);
		cv.put(DbWordCard.Columns.DUE.getName(), wordcard.due);
		cv.put(DbWordCard.Columns.ID_LIST.getName(), wordcard.idList);
		cv.put(DbWordCard.Columns.SYNCINNEXT.getName(), "false");
		Uri uri = ContentUris.withAppendedId(DbWordCard.CONTENT_URI,
				wordcard.idInLocalDB);
		getContentResolver().update(uri, cv, null, null);
	}

	@SuppressLint("SimpleDateFormat")
	private void asyncMarkDeleteWord(final WordCard wordcard) {
		final AsyncTask<WordCard, Void, Void> deleteTask = new AsyncTask<WordCard, Void, Void>() {

			@Override
			protected Void doInBackground(WordCard... wordcards) {
				for (final WordCard wordcard : wordcards) {
					ContentValues cv = new ContentValues();
					int positionList = AccountUtils.getVocabularyListPosition(
							mContext, wordcard.idList);
					if (positionList == VelloConfig.VOCABULARY_LIST_POSITION_8TH) {
						cv.put(DbWordCard.Columns.CLOSED.getName(), "true");
					} else {
						Calendar rightNow = Calendar.getInstance();
						long rightNowUnixTime = rightNow.getTimeInMillis();
						long delta = VelloConfig.VOCABULARY_LIST_DUE_DELTA[positionList];
						long dueUnixTime = rightNowUnixTime + delta;

						SimpleDateFormat format = new SimpleDateFormat(
								"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
						Date dueDate = new Date(dueUnixTime);
						String stringDueDate = format.format(dueDate);
						cv.put(DbWordCard.Columns.DUE.getName(), stringDueDate);

						String newIdList = AccountUtils.getVocabularyListId(
								mContext, positionList + 1);
						cv.put(DbWordCard.Columns.ID_LIST.getName(), newIdList);
					}
					cv.put(DbWordCard.Columns.SYNCINNEXT.getName(), "true");
					Uri uri = ContentUris.withAppendedId(
							DbWordCard.CONTENT_URI, wordcard.idInLocalDB);
					getContentResolver().update(uri, cv, null, null);
				}
				return null;
			}
		};
		mDeletedWord = wordcard;
		mUndoShowing = true;
		deleteTask.execute(wordcard);
		mUndoBar.show(new ActionableToastBar.ActionClickedListener() {
			@Override
			public void onActionClicked() {
				asyncUnmarkDeleteWord(wordcard);
				mDeletedWord = null;
				mUndoShowing = false;
			}
		}, 0, getResources().getString(R.string.word_reviewed), true,
				R.string.word_reviewed_undo, true);
	}

	private void hideUndoBar(boolean animate, MotionEvent event) {
		if (mUndoBar != null) {
			if (event != null && mUndoBar.isEventInToastBar(event)) {
				// Avoid touches inside the undo bar.
				return;
			}
			mUndoBar.hide(animate);
		}
		mDeletedWord = null;
		mUndoShowing = false;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putIntArray(KEY_EXPANDED_IDS, mAdapter.getExpandedArray());
		outState.putParcelable(KEY_DELETED_WORD, mDeletedWord);
		outState.putBoolean(KEY_UNDO_SHOWING, mUndoShowing);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.home, menu);

		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.menu_search)
				.getActionView();
		searchView.setSearchableInfo(searchManager
				.getSearchableInfo(getComponentName()));

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_about:
			return true;
		case R.id.menu_sign_out:
			AccountUtils.signOut(this);
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@SuppressLint("SimpleDateFormat")
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// show all open WordCards whose due time bigger than mobile local now
		// time && syncInNext mark not set
		ProviderCriteria criteria = new ProviderCriteria();
		criteria.addSortOrder(DbWordCard.Columns.DUE, true);
		Calendar rightNow = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		String now = format.format(rightNow.getTime());
		criteria.addLt(DbWordCard.Columns.DUE, now, true);
		criteria.addNe(DbWordCard.Columns.SYNCINNEXT, "true");

		return new CursorLoader(this, DbWordCard.CONTENT_URI,
				DbWordCard.PROJECTION, criteria.getWhereClause(),
				criteria.getWhereParams(), criteria.getOrderClause());
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loder) {
		mAdapter.swapCursor(null);
	}

	class ViewHolder {
		private IcibaWord mWord;
		private String mDesc;
		private IconicTextView mIconicToggleButton;
		private IconicTextView mIconicLifeCount;
		private TextView mTextViewLifeCount;
		private String mIdList;
		private TextView mTextViewKeyword;
		private CharArrayBuffer mCharArrayBufferKeyword;

		private LinearLayout mLinearLayoutPhoneticArea;
		private LinearLayout mLinearLayoutDefinitionArea;

		private Phoneticss p;
		private Definitions d;

		public ViewHolder(View view) {
			mIconicToggleButton = (IconicTextView) view
					.findViewById(R.id.expandable_toggle_button);

			mIconicLifeCount = (IconicTextView) view
					.findViewById(R.id.life_sign);
			mTextViewLifeCount = (TextView) view.findViewById(R.id.life_count);

			mTextViewKeyword = (TextView) view.findViewById(R.id.keyword);
			mCharArrayBufferKeyword = new CharArrayBuffer(20);

			mLinearLayoutPhoneticArea = (LinearLayout) view
					.findViewById(R.id.phonetics_area);
			mLinearLayoutDefinitionArea = (LinearLayout) view
					.findViewById(R.id.definition_area);

		}

		public void populateView(Cursor c) {
			mDesc = c.getString(DbWordCard.Columns.DESC.getIndex());
			mWord = IcibaWordXmlParser.parse(mDesc);
			mIconicToggleButton.setIcon(FontAwesomeIcon.INFO_SIGN);
			mIconicToggleButton.setTextColor(Color.GRAY);

			mIconicLifeCount.setIcon(FontAwesomeIcon.HEART);
			mIconicLifeCount.setTextColor(Color.GRAY);
			mIdList = c.getString(DbWordCard.Columns.ID_LIST.getIndex());
			int positionList = AccountUtils.getVocabularyListPosition(mContext,
					mIdList);
			String lifeString = "N";
			if (positionList != 0) {
				lifeString = String.valueOf(8 - positionList + 1);
			}
			mTextViewLifeCount.setText(lifeString);
			mTextViewLifeCount.setTextColor(Color.GRAY);

			c.copyStringToBuffer(DbWordCard.Columns.NAME.getIndex(),
					mCharArrayBufferKeyword);
			mTextViewKeyword.setText(mCharArrayBufferKeyword.data, 0,
					mCharArrayBufferKeyword.sizeCopied);

			p = mWord.phonetics;
			mLinearLayoutPhoneticArea.removeAllViews();
			for (Phonetics phonetics : p) {
				View phoneticsView = LayoutInflater.from(mContext).inflate(
						R.layout.phonetics_item, null);
				LinearLayout phoneticsGroup = (LinearLayout) phoneticsView
						.findViewById(R.id.phonetics_group);
				((TextView) phoneticsView.findViewById(R.id.phonetics_symbol))
						.setText("[" + phonetics.symbol + "]");
				((IconicTextView) phoneticsView
						.findViewById(R.id.phonetics_sound))
						.setIcon(FontAwesomeIcon.VOLUME_UP);
				((IconicTextView) phoneticsView
						.findViewById(R.id.phonetics_sound))
						.setTextColor(Color.GRAY);
				mLinearLayoutPhoneticArea.addView(phoneticsGroup);
			}

			d = mWord.definition;
			mLinearLayoutDefinitionArea.removeAllViews();
			for (Definition definition : d) {
				View definitionView = LayoutInflater.from(mContext).inflate(
						R.layout.definition_item, null);
				LinearLayout definiitionGroup = (LinearLayout) definitionView
						.findViewById(R.id.definition_group);
				((TextView) definitionView.findViewById(R.id.pos))
						.setText(definition.pos);
				((TextView) definitionView.findViewById(R.id.definiens))
						.setText(definition.definiens);
				mLinearLayoutDefinitionArea.addView(definiitionGroup);
			}
		}
	}

	public class WordCardAdapter extends CursorAdapter {
		private final Context mContext;
		private final LayoutInflater mFactory;
		private final ListView mList;

		private final HashSet<Integer> mExpanded = new HashSet<Integer>();
		private final HashSet<Integer> mSelectedWords = new HashSet<Integer>();
		private final int[] mWordCardBackgroundColor = { R.color.bg_color_new,
				R.color.bg_color_1st, R.color.bg_color_2nd,
				R.color.bg_color_3rd, R.color.bg_color_4th,
				R.color.bg_color_5th, R.color.bg_color_6th,
				R.color.bg_color_7th, R.color.bg_color_8th };

		public class ItemHolder {
			// views for optimization
			LinearLayout wordCardItem;
			IconicTextView iconicLifeCount;
			TextView textViewLifeCount;
			TextView textViewKeyword;

			View expandArea;
			View infoArea;

			LinearLayout linearLayoutPhoneticArea;
			LinearLayout linearLayoutDefinitionArea;

			View hairLine;

			// Other states
			WordCard wordcard;
			IcibaWord word;
			String idList;
			Phoneticss p;
			Definitions d;
		}

		// Used for scrolling an expanded item in the list to make sure it is
		// fully visible.
		private int mScrollWordId = -1;
		private final Runnable mScrollRunnable = new Runnable() {

			@Override
			public void run() {
				if (mScrollWordId != -1) {
					View v = getViewById(mScrollWordId);
					if (v != null) {
						Rect rect = new Rect(v.getLeft(), v.getTop(),
								v.getRight(), v.getBottom());
						mList.requestChildRectangleOnScreen(v, rect, false);
					}
					mScrollWordId = -1;
				}
			}
		};

		public WordCardAdapter(Context context, int[] expandedIds,
				int[] selectedWords, ListView list) {
			super(context, null, 0);
			mContext = context;
			mFactory = LayoutInflater.from(context);
			mList = list;

			Resources res = mContext.getResources();

			if (expandedIds != null) {
				buildHashSetFromArray(expandedIds, mExpanded);
			}

			if (selectedWords != null) {
				buildHashSetFromArray(selectedWords, mSelectedWords);
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (!getCursor().moveToPosition(position)) {
				// May happen if the last word was deleted and the cursor
				// refreshed while the
				// list is updated.
				Log.v(TAG, "couldn't move cursor to position " + position);
				return null;
			}
			View v;
			if (convertView == null) {
				v = newView(mContext, getCursor(), parent);
			} else {
				// Do a translation check to test for animation. Change this to
				// something more
				// reliable and robust in the future.
				if (convertView.getTranslationX() != 0
						|| convertView.getTranslationY() != 0) {
					// view was animated, reset
					v = newView(mContext, getCursor(), parent);
				} else {
					v = convertView;
				}
			}
			bindView(v, mContext, getCursor());
			return v;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			final WordCard wordcard = new WordCard(cursor);
			final ItemHolder itemHolder = (ItemHolder) view.getTag();
			itemHolder.wordcard = wordcard;
			itemHolder.word = IcibaWordXmlParser.parse(wordcard.desc);
			itemHolder.iconicLifeCount.setIcon(FontAwesomeIcon.HEART);
			itemHolder.iconicLifeCount.setTextColor(Color.GRAY);
			itemHolder.idList = itemHolder.wordcard.idList;
			int positionList = AccountUtils.getVocabularyListPosition(mContext,
					itemHolder.idList);
			itemHolder.wordCardItem
					.setBackgroundResource(mWordCardBackgroundColor[positionList]);
			String lifeString = "N";
			if (positionList != 0) {
				lifeString = String.valueOf(8 - positionList + 1);
			}
			itemHolder.textViewLifeCount.setText(lifeString);
			itemHolder.textViewLifeCount.setTextColor(Color.GRAY);

			itemHolder.textViewKeyword.setText(itemHolder.wordcard.name);

			itemHolder.expandArea
					.setVisibility(isWordExpanded(wordcard) ? View.VISIBLE
							: View.GONE);
			itemHolder.infoArea
					.setVisibility(!isWordExpanded(wordcard) ? View.VISIBLE
							: View.GONE);
			itemHolder.infoArea.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					expandWord(itemHolder);
					itemHolder.wordCardItem.post(mScrollRunnable);
				}
			});

			if (isWordExpanded(wordcard)) {
				expandWord(itemHolder);
			}
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			final View view = mFactory.inflate(R.layout.word_card_item, parent,
					false);

			// standard view holder optimization
			final ItemHolder holder = new ItemHolder();
			holder.wordCardItem = (LinearLayout) view
					.findViewById(R.id.word_card_item);
			holder.iconicLifeCount = (IconicTextView) view
					.findViewById(R.id.life_sign);
			holder.textViewLifeCount = (TextView) view
					.findViewById(R.id.life_count);
			holder.textViewKeyword = (TextView) view.findViewById(R.id.keyword);
			holder.expandArea = view.findViewById(R.id.expand_area);
			holder.infoArea = view.findViewById(R.id.info_area);
			holder.linearLayoutPhoneticArea = (LinearLayout) view
					.findViewById(R.id.phonetics_area);
			holder.linearLayoutDefinitionArea = (LinearLayout) view
					.findViewById(R.id.definition_area);
			holder.hairLine = view.findViewById(R.id.hairline);

			view.setTag(holder);
			return view;
		}

		/**
		 * Expands the word for studying.
		 * 
		 * @param itemHolder
		 *            The item holder instance.
		 */
		private void expandWord(ItemHolder itemHolder) {
			itemHolder.expandArea.setVisibility(View.VISIBLE);
			itemHolder.expandArea
					.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							// When action mode is on - simulate long click
							// doLongClick(view);
						}
					});
			itemHolder.infoArea.setVisibility(View.GONE);

			mExpanded.add(itemHolder.wordcard.idInLocalDB);
			bindExpandArea(itemHolder, itemHolder.wordcard);
			// Scroll the view to make sure it is fully viewed
			mScrollWordId = itemHolder.wordcard.idInLocalDB;
		}

		private void bindExpandArea(final ItemHolder itemHolder,
				final WordCard wordcard) {
			// Views in here are not bound until the item is expanded.
			itemHolder.p = itemHolder.word.phonetics;
			itemHolder.linearLayoutPhoneticArea.removeAllViews();
			for (Phonetics phonetics : itemHolder.p) {
				View phoneticsView = LayoutInflater.from(mContext).inflate(
						R.layout.phonetics_item, null);
				LinearLayout phoneticsGroup = (LinearLayout) phoneticsView
						.findViewById(R.id.phonetics_group);
				((TextView) phoneticsView.findViewById(R.id.phonetics_symbol))
						.setText("[" + phonetics.symbol + "]");
				((IconicTextView) phoneticsView
						.findViewById(R.id.phonetics_sound))
						.setIcon(FontAwesomeIcon.VOLUME_UP);
				((IconicTextView) phoneticsView
						.findViewById(R.id.phonetics_sound))
						.setTextColor(Color.GRAY);
				itemHolder.linearLayoutPhoneticArea.addView(phoneticsGroup);
			}

			itemHolder.d = itemHolder.word.definition;
			itemHolder.linearLayoutDefinitionArea.removeAllViews();
			for (Definition definition : itemHolder.d) {
				View definitionView = LayoutInflater.from(mContext).inflate(
						R.layout.definition_item, null);
				LinearLayout definiitionGroup = (LinearLayout) definitionView
						.findViewById(R.id.definition_group);
				((TextView) definitionView.findViewById(R.id.pos))
						.setText(definition.pos);
				((TextView) definitionView.findViewById(R.id.definiens))
						.setText(definition.definiens);
				itemHolder.linearLayoutDefinitionArea.addView(definiitionGroup);
			}
		}

		public void removeSelectedId(int id) {
			mSelectedWords.remove(id);
		}

		private boolean isWordExpanded(WordCard wordcard) {
			return mExpanded.contains(wordcard.idInLocalDB);
		}

		private View getViewById(int id) {
			for (int i = 0; i < mList.getCount(); i++) {
				View v = mList.getChildAt(i);
				if (v != null) {
					ItemHolder h = (ItemHolder) (v.getTag());
					if (h != null && h.wordcard.idInLocalDB == id) {
						return v;
					}
				}
			}
			return null;
		}

		public int[] getExpandedArray() {
			final int[] ids = new int[mExpanded.size()];
			int index = 0;
			for (int id : mExpanded) {
				ids[index] = id;
				index++;
			}
			return ids;
		}

		private void buildHashSetFromArray(int[] ids, HashSet<Integer> set) {
			for (int id : ids) {
				set.add(id);
			}
		}

		public int getSelectedItemsNum() {
			return mSelectedWords.size();
		}
	}

	/*
	 * class GoogleCardsCursorAdapter extends CursorAdapter { public
	 * GoogleCardsCursorAdapter(Context context) { super(context, null, false);
	 * }
	 * 
	 * @Override public void bindView(View view, Context context, Cursor cursor)
	 * { ((ViewHolder) view.getTag()).populateView(cursor);
	 * 
	 * }
	 * 
	 * @Override public View newView(Context context, Cursor arg1, ViewGroup
	 * parent) { View view =
	 * mInflater.inflate(R.layout.activity_googlecards_card, null);
	 * view.setTag(new ViewHolder(view)); return view; } }
	 */

	private void triggerRefresh() {
		Bundle extras = new Bundle();
		extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
		ContentResolver.requestSync(
				new Account(AccountUtils.getChosenAccountName(this),
						Constants.ACCOUNT_TYPE), VelloProvider.AUTHORITY,
				extras);
	}
}
