package com.mili.xiaominglui.app.vello.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.SearchManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.CharArrayBuffer;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
import com.actionbarsherlock.widget.SearchView.OnSuggestionListener;
import com.atermenji.android.iconictextview.IconicTextView;
import com.atermenji.android.iconictextview.icon.FontAwesomeIcon;
import com.devspark.appmsg.AppMsg;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.requestmanager.RequestManager.RequestListener;
import com.haarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.haarman.listviewanimations.itemmanipulation.SwipeDismissAdapter;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
import com.manuelpeinado.refreshactionitem.ProgressIndicatorType;
import com.manuelpeinado.refreshactionitem.RefreshActionItem;
import com.manuelpeinado.refreshactionitem.RefreshActionItem.RefreshActionListener;
import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.config.VelloConfig;
import com.mili.xiaominglui.app.vello.data.factory.IcibaWordXmlParser;
import com.mili.xiaominglui.app.vello.data.model.Board;
import com.mili.xiaominglui.app.vello.data.model.Definition;
import com.mili.xiaominglui.app.vello.data.model.Definitions;
import com.mili.xiaominglui.app.vello.data.model.IcibaWord;
import com.mili.xiaominglui.app.vello.data.model.List;
import com.mili.xiaominglui.app.vello.data.model.Phonetics;
import com.mili.xiaominglui.app.vello.data.model.Phoneticss;
import com.mili.xiaominglui.app.vello.data.model.Word;
import com.mili.xiaominglui.app.vello.data.model.WordCard;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbWordCard;
import com.mili.xiaominglui.app.vello.data.provider.util.ProviderCriteria;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;
import com.mili.xiaominglui.app.vello.dialogs.ConnectionErrorDialogFragment;
import com.mili.xiaominglui.app.vello.dialogs.ConnectionErrorDialogFragment.ConnectionErrorDialogListener;
import com.mili.xiaominglui.app.vello.service.VelloService;
import com.mili.xiaominglui.app.vello.util.AccountUtils;
import com.tjerkw.slideexpandable.library.SlideExpandableListAdapter;

public class MainActivity extends BaseActivity implements RequestListener,
		ConnectionErrorDialogListener, RefreshActionListener,
		OnQueryTextListener, OnSuggestionListener, LoaderCallbacks<Cursor> {
	private static final String TAG = MainActivity.class.getSimpleName();
	private Context mContext;

	private GoogleCardsCursorAdapter mGoogleCardsAdapter;
	private RefreshActionItem mRefreshActionItem;

	private SuggestionsAdapter mSuggestionsAdapter;
	private static final String[] COLUMNS = { BaseColumns._ID,
			SearchManager.SUGGEST_COLUMN_TEXT_1, };

	private class SuggestionsAdapter extends CursorAdapter {

		public SuggestionsAdapter(Context context, Cursor c) {
			super(context, c, 0);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			LayoutInflater inflater = LayoutInflater.from(context);
			View v = inflater.inflate(android.R.layout.simple_list_item_1,
					parent, false);
			return v;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			TextView tv = (TextView) view;
			final int textIndex = cursor
					.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1);
			tv.setText(cursor.getString(textIndex));
		}
	}

	private LayoutInflater mInflater;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getApplicationContext();
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		if (isFinishing()) {
			return;
		}

		setContentView(R.layout.activity_main);
		ListView listView = (ListView) findViewById(R.id.activity_googlecards_listview);
		// mGoogleCardsAdapter = new GoogleCardsCursorAdapter(this);
		mGoogleCardsAdapter = new GoogleCardsCursorAdapter(this);
		SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(
				new SwipeDismissAdapter(mGoogleCardsAdapter,
						new MyOnDismissCallback(mGoogleCardsAdapter)));
		swingBottomInAnimationAdapter.setListView(listView);

		SlideExpandableListAdapter slideExpandableListAdapter = new SlideExpandableListAdapter(
				swingBottomInAnimationAdapter, R.id.expandable_toggle_button,
				R.id.expandable);

		listView.setAdapter(slideExpandableListAdapter);

		getSupportLoaderManager().initLoader(0, null, this);
		mInflater = getLayoutInflater();
	}

	private class MyOnDismissCallback implements OnDismissCallback {

		private GoogleCardsCursorAdapter mAdapter;

		public MyOnDismissCallback(GoogleCardsCursorAdapter adapter) {
			mAdapter = adapter;
		}

		@Override
		public void onDismiss(ListView listView, int[] reverseSortedPositions) {
			Cursor c = mAdapter.getCursor();
			for (int position : reverseSortedPositions) {
				c.moveToPosition(position);
				int id = c.getInt(DbWordCard.Columns.ID.getIndex());
				String idList = c.getString(DbWordCard.Columns.ID_LIST
						.getIndex());
				int positionList = AccountUtils.getVocabularyListPosition(
						mContext, idList);

				ContentValues cv = new ContentValues();
				if (positionList == VelloConfig.VOCABULARY_LIST_POSITION_8TH) {
					cv.put(DbWordCard.Columns.CLOSED.getName(), "true");

				} else {
					Calendar rightNow = Calendar.getInstance();
					long rightNowUnixTime = rightNow.getTimeInMillis();
					long delta = VelloConfig.VOCABULARY_LIST_DUE_DELTA[position];
					long dueUnixTime = rightNowUnixTime + delta;

					SimpleDateFormat format = new SimpleDateFormat(
							"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
					Date dueDate = new Date(dueUnixTime);
					String stringDueDate = format.format(dueDate);
					cv.put(DbWordCard.Columns.DUE.getName(), stringDueDate);

					String newIdList = AccountUtils.getVocabularyListId(
							mContext, position + 1);
					cv.put(DbWordCard.Columns.ID_LIST.getName(), newIdList);
				}
				Uri uri = ContentUris
						.withAppendedId(DbWordCard.CONTENT_URI, id);
				getContentResolver().update(uri, cv, null, null);
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		for (int i = 0; i < mRequestList.size(); i++) {
			Request request = mRequestList.get(i);

			if (mRequestManager.isRequestInProgress(request)) {
				mRequestManager.addRequestListener(this, request);
			} else {
				mRequestManager.callListenerWithCachedData(this, request);
				i--;
				mRequestList.remove(request);
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (!mRequestList.isEmpty()) {
			mRequestManager.removeRequestListener(this);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getSupportMenuInflater().inflate(R.menu.home, menu);
		MenuItem item = menu.findItem(R.id.refresh_button);
		mRefreshActionItem = (RefreshActionItem) item.getActionView();
		mRefreshActionItem.setMenuItem(item);
		mRefreshActionItem
				.setProgressIndicatorType(ProgressIndicatorType.INDETERMINATE);
		mRefreshActionItem.setRefreshActionListener(this);

		SearchView searchView = new SearchView(getSupportActionBar()
				.getThemedContext());
		searchView.setQueryHint(getResources().getText(
				R.string.action_query_hint));
		searchView.setOnQueryTextListener(this);
		/*
		 * searchView.setOnSuggestionListener(this); if (mSuggestionsAdapter ==
		 * null) { MatrixCursor cursor = new MatrixCursor(COLUMNS);
		 * cursor.addRow(new String[] { "1", "apple" }); cursor.addRow(new
		 * String[] { "2", "word" }); cursor.addRow(new String[] { "3", "show"
		 * }); mSuggestionsAdapter = new
		 * SuggestionsAdapter(getSupportActionBar() .getThemedContext(),
		 * cursor); } searchView.setSuggestionsAdapter(mSuggestionsAdapter);
		 */

		boolean isLight = false;
		menu.add(Menu.NONE, 0, 97, R.string.description_search)
				.setIcon(
						isLight ? R.drawable.ic_search_inverse
								: R.drawable.abs__ic_search)
				.setActionView(searchView)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

		if (AccountUtils.hasVocabularyBoard(mContext)
				&& AccountUtils.isVocabularyBoardWellFormed(mContext)) {
			// all initialized
			// getDueWordCardList();
			// getAllWordCardList();
		} else {
			// begin to check vocabulary board
			checkVocabularyBoard();
		}

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

	@Override
	public void onRequestFinished(Request request, Bundle resultData) {
		mRefreshActionItem.showProgress(false);
		if (mRequestList.contains(request)) {
			mRequestList.remove(request);

			switch (request.getRequestType()) {
			case VelloRequestFactory.REQUEST_TYPE_CHECK_VOCABULARY_BOARD:
				ArrayList<Board> boardList = resultData
						.getParcelableArrayList(VelloRequestFactory.BUNDLE_EXTRA_TRELLO_BOARD_LIST);
				for (Board board : boardList) {
					if (board.name.equals(AccountUtils
							.getVocabularyBoardName(mContext))
							&& board.desc.equals(AccountUtils
									.getVocabularyBoardVerification())) {
						// s - 0 find out vocabulary board, check the closed
						// flag.

						if (!board.closed.equals("true")) {
							// s - 0.0 vocabulary board is NOT well
							// configured.
							configureVocabularyBoard(board.id);
							return;
						} else {
							// s - 0.1 well configured vocabulary board,
							// save id
							AccountUtils.setVocabularyBoardId(mContext,
									board.id);
							checkVocabularyLists();
							return;
						}
					}
				}

				// s - 1 NO vocabulary board found, need create one
				createVocabularyBoard();
				if (VelloConfig.DEBUG_SWITCH) {
					Log.d(TAG, "check vocabulary board end.");
				}
				return;

			case VelloRequestFactory.REQUEST_TYPE_CONFIGURE_VOCABULARY_BOARD:
				String boardId = request
						.getString(VelloRequestFactory.PARAM_EXTRA_VOCABULARY_BOARD_ID);
				if (resultData != null) {
					// configure board successfully & save it
					String id = resultData
							.getString(VelloRequestFactory.BUNDLE_EXTRA_VOCABULARY_BOARD_ID);
					AccountUtils.setVocabularyBoardId(mContext, id);
					Log.d(TAG,
							"configure board successfully! vocabulary board id = "
									+ id);

					// continue to check vocabulary list if not well formed
					checkVocabularyLists();
				} else {
					// configure board failed, try again
					configureVocabularyBoard(boardId);
				}
				return;

			case VelloRequestFactory.REQUEST_TYPE_CHECK_VOCABULARY_LIST:
				ArrayList<List> listList = resultData
						.getParcelableArrayList(VelloRequestFactory.BUNDLE_EXTRA_VOCABULARY_LIST_LIST);
				int position = request
						.getInt(VelloRequestFactory.PARAM_EXTRA_VOCABULARY_LIST_POSITION);
				for (List list : listList) {
					if (list.name
							.equals(AccountUtils.VOCABULARY_LISTS_TITLE_ID[position])) {
						// get the vocabulary list[position]
						if (!list.closed.equals("false")) {
							// list[position] be closed unexpectedly
							reOpenVocabulayList(position, list.id);
							return;
						} else {
							// list[position]'s status ok, save listId
							AccountUtils.setVocabularyListId(mContext, list.id,
									position);
							if (AccountUtils
									.isVocabularyBoardWellFormed(mContext)) {
								AppMsg.makeText(this,
										R.string.toast_init_vocabulary_end,
										AppMsg.STYLE_INFO)
										.setLayoutGravity(Gravity.BOTTOM)
										.show();
								// getDueWordCardList();
							}
							return;
						}

					}

				}
				// no vocabulary list found
				createVocabularyList(position);
				if (VelloConfig.DEBUG_SWITCH) {
					Log.d(TAG, "check vocabulary list end.");
				}
				return;

			case VelloRequestFactory.REQUEST_TYPE_REOPEN_VOCABULARY_LIST:
				int positonList = request
						.getInt(VelloRequestFactory.PARAM_EXTRA_VOCABULARY_LIST_POSITION);
				String idList = request
						.getString(VelloRequestFactory.BUNDLE_EXTRA_VOCABULARY_LIST_ID);
				if (resultData != null) {
					// reopen list successfully
					String id = resultData
							.getString(VelloRequestFactory.BUNDLE_EXTRA_VOCABULARY_LIST_ID);
					int pos = resultData
							.getInt(VelloRequestFactory.PARAM_EXTRA_VOCABULARY_LIST_POSITION);
					AccountUtils.setVocabularyListId(mContext, id, pos);
					if (AccountUtils.isVocabularyBoardWellFormed(mContext)) {
						AppMsg.makeText(this,
								R.string.toast_init_vocabulary_end,
								AppMsg.STYLE_INFO)
								.setLayoutGravity(Gravity.BOTTOM).show();
						// getDueWordCardList();
					}
				} else {
					// reopen failed, try again
					reOpenVocabulayList(positonList, idList);
				}
				return;

			case VelloRequestFactory.REQUEST_TYPE_CREATE_VOCABULARY_LIST:
				int positionList = request
						.getInt(VelloRequestFactory.PARAM_EXTRA_VOCABULARY_LIST_POSITION);
				if (resultData != null) {
					// create list successfully
					String id = resultData
							.getString(VelloRequestFactory.BUNDLE_EXTRA_VOCABULARY_LIST_ID);
					int pos = resultData
							.getInt(VelloRequestFactory.PARAM_EXTRA_VOCABULARY_LIST_POSITION);
					AccountUtils.setVocabularyListId(mContext, id, pos);
					if (AccountUtils.isVocabularyBoardWellFormed(mContext)) {
						AppMsg.makeText(this,
								R.string.toast_init_vocabulary_end,
								AppMsg.STYLE_INFO)
								.setLayoutGravity(Gravity.BOTTOM).show();
						// getDueWordCardList();
					}
				} else {
					// create list failed, try again
					createVocabularyList(positionList);
				}
				if (VelloConfig.DEBUG_SWITCH) {
					Log.d(TAG, "create vocabulary list end.");
				}
				return;

			case VelloRequestFactory.REQUEST_TYPE_CREATE_VOCABULARY_BOARD:
				if (resultData != null) {
					// create vocabulary board successfully
					String id = resultData
							.getString(VelloRequestFactory.BUNDLE_EXTRA_VOCABULARY_BOARD_ID);

					configureVocabularyBoard(id);
				} else {
					// create vocabulary board failed, try again
					createVocabularyBoard();
				}
				return;

			case VelloRequestFactory.REQUEST_TYPE_GET_DUE_WORDCARD_LIST:
				ArrayList<WordCard> wordCardList = resultData
						.getParcelableArrayList(VelloRequestFactory.BUNDLE_EXTRA_WORDCARD_LIST);

				if (wordCardList.size() > 0) {
					// show word cards need review
					for (WordCard wordCard : wordCardList) {
						new WordCardToWordTask().execute(wordCard);
					}
				} else {
					// NO word card need review
					AppMsg.makeText(this, R.string.toast_no_word_now,
							AppMsg.STYLE_CONFIRM).setLayoutGravity(Gravity.TOP)
							.show();
				}

				if (VelloConfig.DEBUG_SWITCH) {
					Log.d(TAG, "get due word card list end.");
				}
				return;

			case VelloRequestFactory.REQUEST_TYPE_LOOK_UP_WORD:
				String wsResult = resultData
						.getString(VelloRequestFactory.BUNDLE_EXTRA_DICTIONARY_ICIBA_RESPONSE);
				String keyword = request
						.getString(VelloRequestFactory.PARAM_EXTRA_QUERY_WORD_KEYWORD);

				IcibaWord word = IcibaWordXmlParser.parse(wsResult);
				if (word.definition.size() > 0) {
					// response a available word and save to trello
					// begin save word flow
					checkWordCardStatus(keyword, wsResult);

				} else {
					// NOT available word, tell user the truth.
					AppMsg.makeText(this, R.string.toast_not_available_word,
							AppMsg.STYLE_ALERT).setLayoutGravity(Gravity.TOP)
							.show();
				}

				if (VelloConfig.DEBUG_SWITCH) {
					Log.d(TAG, "look up word end.");
				}

				return;

			case VelloRequestFactory.REQUEST_TYPE_CHECK_WORDCARD_STATUS:
				ArrayList<WordCard> existedWordCardList = resultData
						.getParcelableArrayList(VelloRequestFactory.BUNDLE_EXTRA_WORDCARD_LIST);
				String newWord = request
						.getString(VelloRequestFactory.PARAM_EXTRA_QUERY_WORD_KEYWORD);
				String newWordResult = request
						.getString(VelloRequestFactory.PARAM_EXTRA_CHECK_WORDCARD_WS_RESULT);
				if (existedWordCardList.isEmpty()) {
					// new word, should add WordCard
					if (VelloConfig.DEBUG_SWITCH) {
						Log.d(TAG, "new word, should add WordCard");
					}
					addWordCard(newWord, newWordResult);
				} else {
					// check more to decide
					// filter to find the right one
					for (WordCard w : existedWordCardList) {
						if (w.name.equals(newWord)) {
							// got the right word card
							// show with user first
							new WordCardToWordTask().execute(w);

							if (w.closed.equals("true")) {
								// the existed word card has be closed,
								// re-open
								// it.
								if (VelloConfig.DEBUG_SWITCH) {
									Log.d(TAG, "re-open existed word card.");
								}
								reOpenWordCard(w.idCard);
							} else {
								if (w.due.equals("null")) {
									// the existed word card has not be
									// initialized, initialize it. this is
									// the
									// double check
									if (VelloConfig.DEBUG_SWITCH) {
										Log.d(TAG,
												"initialize existed word card.");
									}
									initializeWordCard(w.idCard);
								} else {
									// the existed word is in review
									// process, do
									// nothing at present.
									if (VelloConfig.DEBUG_SWITCH) {
										Log.d(TAG,
												"the existed word is in review.");
									}
								}
							}
							if (VelloConfig.DEBUG_SWITCH) {
								Log.d(TAG, "check wordcard status end.");
							}
							showCurrentBadge();
							return;
						}
					}

					addWordCard(newWord, newWordResult);
				}
				if (VelloConfig.DEBUG_SWITCH) {
					Log.d(TAG, "check wordcard status end.");
				}
				return;

			case VelloRequestFactory.REQUEST_TYPE_ADD_WORDCARD:
				WordCard addedWordCard = resultData
						.getParcelable(VelloRequestFactory.BUNDLE_EXTRA_WORDCARD);
				if (addedWordCard != null) {
					// show added wordcard to user
					new WordCardToWordTask().execute(addedWordCard);
					// at the same time initialize it.
					initializeWordCard(addedWordCard.idCard);
				} else {
					// add failed
					// do nothing at present
				}

				if (VelloConfig.DEBUG_SWITCH) {
					Log.d(TAG, "addWordCard end.");
				}

				return;

			case VelloRequestFactory.REQUEST_TYPE_REOPEN_WORDCARD:
				WordCard reopenedWordCard = resultData
						.getParcelable(VelloRequestFactory.BUNDLE_EXTRA_WORDCARD);
				if (reopenedWordCard != null) {
					// reopened
					// do nothing at present.
				} else {
					// reopen failed
					// do nothing at present.
				}
				if (VelloConfig.DEBUG_SWITCH) {
					Log.d(TAG, "reOpenzeWordCard end.");
				}
				return;

			case VelloRequestFactory.REQUEST_TYPE_INITIALIZE_WORDCARD:
				WordCard initializedWordCard = resultData
						.getParcelable(VelloRequestFactory.BUNDLE_EXTRA_WORDCARD);
				if (initializedWordCard != null) {
					// initialized
					// should show to user
					// TODO
				} else {
					// initialized failed
					// do nothing at present
				}
				if (VelloConfig.DEBUG_SWITCH) {
					Log.d(TAG, "initializeWordCard end.");
				}
				return;

			case VelloRequestFactory.REQUEST_TYPE_REVIEWED_WORDCARD:
				WordCard reviewedWordCard = resultData
						.getParcelable(VelloRequestFactory.BUNDLE_EXTRA_WORDCARD);
				if (reviewedWordCard != null) {
					// reviewed

				} else {
					// reviewed failed
					// do nothing at present
				}
				showCurrentBadge();
				if (VelloConfig.DEBUG_SWITCH) {
					Log.d(TAG, "reviewedWordCard end.");
				}
				return;

			default:
				return;
			}
		}
	}

	@Override
	public void onRequestConnectionError(Request request, int statusCode) {
		if (mRequestList.contains(request)) {
			setProgressBarIndeterminateVisibility(false);
			mRequestList.remove(request);

			ConnectionErrorDialogFragment.show(this, request, this);
		}

	}

	@Override
	public void onRequestDataError(Request request) {
		if (mRequestList.contains(request)) {
			mRefreshActionItem.showProgress(false);
			mRequestList.remove(request);

			showBadDataErrorDialog();
		}
	}

	@Override
	public void onRequestCustomError(Request request, Bundle resultData) {
		// Never called.
	}

	private void checkVocabularyBoard() {
		mRefreshActionItem.showProgress(true);
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "checkVocabularyBoard start...");
		}
		AppMsg.makeText(this, R.string.toast_init_vocabulary_start,
				AppMsg.STYLE_INFO).setLayoutGravity(Gravity.BOTTOM).show();
		Request checkVocabularyBoardRequest = VelloRequestFactory
				.checkVocabularyBoardRequest();
		mRequestManager.execute(checkVocabularyBoardRequest, this);
		mRequestList.add(checkVocabularyBoardRequest);
	}

	private void createVocabularyBoard() {
		mRefreshActionItem.showProgress(true);
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "createVocabularyBoard start...");
		}
		Request createVocabularyBoardRequest = VelloRequestFactory
				.createVocabularyBoardRequest();
		mRequestManager.execute(createVocabularyBoardRequest, this);
		mRequestList.add(createVocabularyBoardRequest);
	}

	private void createVocabularyList(int position) {
		mRefreshActionItem.showProgress(true);
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "createVocabularyList start...");
		}
		Request createVocabularyListRequest = VelloRequestFactory
				.createVocabularyListRequest(position);
		mRequestManager.execute(createVocabularyListRequest, this);
		mRequestList.add(createVocabularyListRequest);
	}

	private void reOpenVocabulayList(int position, String id) {
		mRefreshActionItem.showProgress(true);
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "reOpenVocabulayList start...");
		}
		Request reOpenVocabularyListRequest = VelloRequestFactory
				.reOpenVocabularyListRequest(position, id);
		mRequestManager.execute(reOpenVocabularyListRequest, this);
		mRequestList.add(reOpenVocabularyListRequest);
	}

	private void checkVocabularyLists() {
		mRefreshActionItem.showProgress(true);

		for (int i = 0; i < AccountUtils.VOCABULARY_LISTS_TITLE_ID.length; i++) {
			if (VelloConfig.DEBUG_SWITCH) {
				Log.d(TAG, "checkVocabularyLists start..." + i);
			}
			Request checkVocabularyListReqest = VelloRequestFactory
					.checkVocabularyListRequest(i);

			mRequestManager.execute(checkVocabularyListReqest, this);
			mRequestList.add(checkVocabularyListReqest);
		}

	}

	private void configureVocabularyBoard(String id) {
		mRefreshActionItem.showProgress(true);
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "configureVocabularyBoard start...");
		}
		Request configureVocabularyBoardRequest = VelloRequestFactory
				.configureVocabularyBoardRequest(id);
		mRequestManager.execute(configureVocabularyBoardRequest, this);
		mRequestList.add(configureVocabularyBoardRequest);
	}

	private void getDueWordCardList() {
		mRefreshActionItem.showProgress(true);
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "getDueWordCardList start...");
		}

		AppMsg.makeText(this, R.string.toast_get_due_word, AppMsg.STYLE_INFO)
				.setLayoutGravity(Gravity.BOTTOM).show();
		Request getDueWordCardListRequest = VelloRequestFactory
				.getDueWordCardListRequest();
		mRequestManager.execute(getDueWordCardListRequest, this);
		mRequestList.add(getDueWordCardListRequest);
	}

	private void getAllWordCardList() {
		mRefreshActionItem.showProgress(true);
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "getAllWordCardList start ...");
		}

		AppMsg.makeText(this, R.string.toast_get_due_word, AppMsg.STYLE_INFO)
				.setLayoutGravity(Gravity.BOTTOM).show();
		Request getAllWordCardListRequest = VelloRequestFactory
				.getAllWordCardListRequest();
		mRequestManager.execute(getAllWordCardListRequest, this);
		mRequestList.add(getAllWordCardListRequest);

	}

	private void lookUpWord(String keyword) {
		mRefreshActionItem.showProgress(true);
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "look up word start...");
		}
		Request lookUpWordRequest = VelloRequestFactory
				.lookUpWordRequest(keyword);
		mRequestManager.execute(lookUpWordRequest, this);
		mRequestList.add(lookUpWordRequest);

	}

	private void checkWordCardStatus(String keyword, String wsResult) {
		mRefreshActionItem.showProgress(true);
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "checkWordCardStatusRequest start...");
		}
		Request checkWordCardStatus = VelloRequestFactory
				.checkWordCardStatusRequest(keyword, wsResult);
		mRequestManager.execute(checkWordCardStatus, this);
		mRequestList.add(checkWordCardStatus);
	}

	private void addWordCard(String keyword, String data) {
		mRefreshActionItem.showProgress(true);
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "addWordCard start...");
		}
		Request addWordCard = VelloRequestFactory.addWordCardRequest(keyword,
				data);
		mRequestManager.execute(addWordCard, this);
		mRequestList.add(addWordCard);
	}

	private void initializeWordCard(String idCard) {
		mRefreshActionItem.showProgress(true);
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "initializeWordCard start...");
		}
		Request initializeWordCard = VelloRequestFactory
				.initializeWordCardRequest(idCard);
		mRequestManager.execute(initializeWordCard, this);
		mRequestList.add(initializeWordCard);
	}

	private void reOpenWordCard(String idCard) {
		mRefreshActionItem.showProgress(true);
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "reOpenWordCard start...");
		}
		Request reOpenWordCard = VelloRequestFactory
				.reOpenWordCardRequest(idCard);
		mRequestManager.execute(reOpenWordCard, this);
		mRequestList.add(reOpenWordCard);
	}

	private void archiveWordCard(String idCard) {
		// TODO
	}

	private void reviewedWordCard(String idCard, int position) {
		mRefreshActionItem.showProgress(true);
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "reviewedWordCard start...");
		}
		Request reviewedWordCard = VelloRequestFactory.reviewedWordCardRequest(
				idCard, position);
		mRequestManager.execute(reviewedWordCard, this);
		mRequestList.add(reviewedWordCard);
	}

	@Override
	public void connectionErrorDialogCancel(Request request) {
		mRefreshActionItem.showProgress(false);
	}

	@Override
	public void connectionErrorDialogRetry(Request request) {
		mRequestManager.execute(request, this);
		mRequestList.add(request);
	}

	@Override
	public void onRefreshButtonClick(RefreshActionItem sender) {
		// mGoogleCardsAdapter.clear();
		// getDueWordCardList();
//		getAllWordCardList();
		Intent intent = new Intent(this, VelloService.class);
		startService(intent);
	}

	private class WordCardToWordTask extends
			AsyncTask<WordCard, Integer, ArrayList<Word>> {

		@Override
		protected ArrayList<Word> doInBackground(WordCard... wordcards) {

			ArrayList<Word> wordList = new ArrayList<Word>();
			for (WordCard wordcard : wordcards) {
				IcibaWord word = new IcibaWord();
				String xmlWord = wordcard.desc;
				word = IcibaWordXmlParser.parse(xmlWord);

				if (word == null) {
					word = new IcibaWord();
				}
				word.idCard = wordcard.idCard;
				word.idList = wordcard.idList;
				word.keyword = wordcard.name;
				word.due = wordcard.due;
				wordList.add(word);
			}
			return wordList;
		}

		@Override
		protected void onPostExecute(ArrayList<Word> result) {
			// mGoogleCardsAdapter.addAll(result);
			showCurrentBadge();
			mRefreshActionItem.showProgress(false);
		}
	}

	private void showCurrentBadge() {
		int num = mGoogleCardsAdapter.getCount();
		if (num > 0) {
			mRefreshActionItem.showBadge(String.valueOf(num));
		} else {
			mRefreshActionItem.hideBadge();
		}
	}

	@Override
	public boolean onSuggestionSelect(int position) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onSuggestionClick(int position) {
		Cursor c = (Cursor) mSuggestionsAdapter.getItem(position);
		String query = c.getString(c
				.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
		Toast.makeText(this, "Suggestion clicked: " + query, Toast.LENGTH_LONG)
				.show();
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		if (query != null) {
			lookUpWord(query.trim().toLowerCase());
		}
		return true;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		ProviderCriteria criteria = new ProviderCriteria();
		criteria.addSortOrder(DbWordCard.Columns.DUE, true);

		Calendar rightNow = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		String now = format.format(rightNow.getTime());
		criteria.addLt(DbWordCard.Columns.DUE, now, true);

		criteria.addNe(DbWordCard.Columns.CLOSED, "true");

		return new CursorLoader(this, DbWordCard.CONTENT_URI,
				DbWordCard.PROJECTION, criteria.getWhereClause(),
				criteria.getWhereParams(), criteria.getOrderClause());
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mGoogleCardsAdapter.changeCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loder) {
		mGoogleCardsAdapter.changeCursor(null);
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
			mTextViewLifeCount.setText(String.valueOf(8 - positionList));
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

	class GoogleCardsCursorAdapter extends CursorAdapter {
		public GoogleCardsCursorAdapter(Context context) {
			super(context, null, false);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			((ViewHolder) view.getTag()).populateView(cursor);

		}

		@Override
		public View newView(Context context, Cursor arg1, ViewGroup parent) {
			View view = mInflater.inflate(R.layout.activity_googlecards_card,
					null);
			view.setTag(new ViewHolder(view));
			return view;
		}
	}
}
