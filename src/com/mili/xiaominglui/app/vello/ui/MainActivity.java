package com.mili.xiaominglui.app.vello.ui;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.requestmanager.RequestManager.RequestListener;
import com.haarman.listviewanimations.ArrayAdapter;
import com.haarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.haarman.listviewanimations.itemmanipulation.SwipeDismissAdapter;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
import com.manuelpeinado.refreshactionitem.ProgressIndicatorType;
import com.manuelpeinado.refreshactionitem.RefreshActionItem;
import com.manuelpeinado.refreshactionitem.RefreshActionItem.RefreshActionListener;
import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.adapter.GoogleCardsAdapter;
import com.mili.xiaominglui.app.vello.data.factory.IcibaWordXmlParser;
import com.mili.xiaominglui.app.vello.data.model.Board;
import com.mili.xiaominglui.app.vello.data.model.IcibaWord;
import com.mili.xiaominglui.app.vello.data.model.List;
import com.mili.xiaominglui.app.vello.data.model.Word;
import com.mili.xiaominglui.app.vello.data.model.WordCard;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;
import com.mili.xiaominglui.app.vello.dialogs.ConnectionErrorDialogFragment.ConnectionErrorDialogListener;
import com.mili.xiaominglui.app.vello.util.AccountUtils;
import com.tjerkw.slideexpandable.library.SlideExpandableListAdapter;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends BaseActivity implements RequestListener,
	ConnectionErrorDialogListener, OnDismissCallback, RefreshActionListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private Menu mOptionsMenu;
    private Context mContext;

    private GoogleCardsAdapter mGoogleCardsAdapter;
    private RefreshActionItem mRefreshActionItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	mContext = getApplicationContext();
	requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

	if (isFinishing()) {
	    return;
	}

	if (!AccountUtils.hasVocabularyBoard(mContext)) {
	    // vocabulary board id not found
	    checkVocabularyBoard();
	}

	if (!AccountUtils.isVocabularyBoardWellFormed(mContext)) {
	    // checkVocabularyLists();
	}

	setContentView(R.layout.activity_main);
	ListView listView = (ListView) findViewById(R.id.activity_googlecards_listview);
	mGoogleCardsAdapter = new GoogleCardsAdapter(this);
	SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(
		new SwipeDismissAdapter(mGoogleCardsAdapter,
			new MyOnDismissCallback(mGoogleCardsAdapter)));
	swingBottomInAnimationAdapter.setListView(listView);
	
	SlideExpandableListAdapter slideExpandableListAdapter = new SlideExpandableListAdapter(swingBottomInAnimationAdapter, R.id.expandable_toggle_button, R.id.expandable);

	listView.setAdapter(slideExpandableListAdapter);

    }

    private class MyOnDismissCallback implements OnDismissCallback {

	private ArrayAdapter<Word> mAdapter;

	public MyOnDismissCallback(ArrayAdapter<Word> adapter) {
	    mAdapter = adapter;
	}

	@Override
	public void onDismiss(ListView listView, int[] reverseSortedPositions) {
	    for (int position : reverseSortedPositions) {
		// handle review word request here
		// TODO
		mAdapter.remove(position);
	    }
	    Toast.makeText(
		    mContext,
		    "Removed positions: "
			    + Arrays.toString(reverseSortedPositions),
		    Toast.LENGTH_SHORT).show();
	}
    }

    @Override
    protected void onResume() {
	super.onResume();
	for (int i = 0; i < mRequestList.size(); i++) {
	    Request request = mRequestList.get(i);

	    if (mRequestManager.isRequestInProgress(request)) {
		mRequestManager.addRequestListener(this, request);
		mRefreshActionItem.showProgress(true);
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
	mOptionsMenu = menu;
	getSupportMenuInflater().inflate(R.menu.home, menu);
	MenuItem item = menu.findItem(R.id.refresh_button);
	mRefreshActionItem = (RefreshActionItem) item.getActionView();
	mRefreshActionItem.setMenuItem(item);
	mRefreshActionItem.setProgressIndicatorType(ProgressIndicatorType.INDETERMINATE);
	mRefreshActionItem.setRefreshActionListener(this);
	getDueWordCardList();
	// setupSearchMenuItem(menu);
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
			// find out vocabulary board, check the closed flag.

			if (!board.closed.equals("true")) {
			    // vocabulary board is not well configured.
			    configureVocabularyBoard(board.id);
			    return;
			} else {
			    // well configured vocabulary board, save id &
			    // checking the lists settings
			    AccountUtils.setVocabularyBoardId(mContext,
				    board.id);
			    checkVocabularyLists();
			    return;
			}
		    }
		}

		// no vocabulary board found, need create one
		createVocabularyBoard();
		return;

	    case VelloRequestFactory.REQUEST_TYPE_CONFIGURE_VOCABULARY_BOARD:
		if (resultData != null) {
		    // configure board successfully & save it
		    String id = resultData
			    .getString(VelloRequestFactory.BUNDLE_EXTRA_VOCABULARY_BOARD_ID);
		    AccountUtils.setVocabularyBoardId(mContext, id);
		    Log.d(TAG,
			    "configure board successfully! vocabulary board id = "
				    + id);
		}

		// continue to check vocabulary list if not well formed
		checkVocabularyLists();
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
			    return;
			}

		    }

		}

		// no vocabulary list found
		createVocabularyList(position);
		return;

	    case VelloRequestFactory.REQUEST_TYPE_REOPEN_VOCABULARY_LIST:
		if (resultData != null) {
		    // reopen list successfully
		    String id = resultData
			    .getString(VelloRequestFactory.BUNDLE_EXTRA_VOCABULARY_LIST_ID);
		    int pos = resultData
			    .getInt(VelloRequestFactory.PARAM_EXTRA_VOCABULARY_LIST_POSITION);
		    AccountUtils.setVocabularyListId(mContext, id, pos);
		    Log.d(TAG,
			    "reopen list successfully! vocabulary list id = "
				    + id);
		}
		return;

	    case VelloRequestFactory.REQUEST_TYPE_CREATE_VOCABULARY_LIST:
		if (resultData != null) {
		    // create list successfully
		    String id = resultData
			    .getString(VelloRequestFactory.BUNDLE_EXTRA_VOCABULARY_LIST_ID);
		    int pos = resultData
			    .getInt(VelloRequestFactory.PARAM_EXTRA_VOCABULARY_LIST_POSITION);
		    AccountUtils.setVocabularyListId(mContext, id, pos);
		    Log.d(TAG,
			    "create list successfully! vocabulary list id = "
				    + id);
		}
		return;

	    case VelloRequestFactory.REQUEST_TYPE_CREATE_VOCABULARY_BOARD:
		if (resultData != null) {
		    // create vocabulary board successfully
		    String id = resultData
			    .getString(VelloRequestFactory.BUNDLE_EXTRA_VOCABULARY_BOARD_ID);

		    configureVocabularyBoard(id);
		}
		return;

	    case VelloRequestFactory.REQUEST_TYPE_GET_DUE_WORDCARD_LIST:
		ArrayList<WordCard> wordCardList = resultData
			.getParcelableArrayList(VelloRequestFactory.BUNDLE_EXTRA_WORDCARD_LIST);
		
		for (WordCard wordCard : wordCardList) {
		    new WordCardToWordTask().execute(wordCard);
		}
		return;
	    default:
		return;
	    }
	}
    }

    @Override
    public void onRequestConnectionError(Request request, int statusCode) {

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
	Request checkVocabularyBoardRequest = VelloRequestFactory
		.checkVocabularyBoardRequest();
	mRequestManager.execute(checkVocabularyBoardRequest, this);
	mRequestList.add(checkVocabularyBoardRequest);
    }

    private void createVocabularyBoard() {
	mRefreshActionItem.showProgress(true);
	Request createVocabularyBoardRequest = VelloRequestFactory
		.createVocabularyBoardRequest();
	mRequestManager.execute(createVocabularyBoardRequest, this);
	mRequestList.add(createVocabularyBoardRequest);
    }

    private void createVocabularyList(int position) {
	mRefreshActionItem.showProgress(true);
	Request createVocabularyListRequest = VelloRequestFactory
		.createVocabularyListRequest(position);
	mRequestManager.execute(createVocabularyListRequest, this);
	mRequestList.add(createVocabularyListRequest);
    }

    private void reOpenVocabulayList(int position, String id) {
	mRefreshActionItem.showProgress(true);
	Request reOpenVocabularyListRequest = VelloRequestFactory
		.reOpenVocabularyListRequest(position, id);
	mRequestManager.execute(reOpenVocabularyListRequest, this);
	mRequestList.add(reOpenVocabularyListRequest);
    }

    private void checkVocabularyLists() {
	mRefreshActionItem.showProgress(true);
	for (int i = 0; i < AccountUtils.VOCABULARY_LISTS_TITLE_ID.length; i++) {
	    Request checkVocabularyListReqest = VelloRequestFactory
		    .checkVocabularyListRequest(i);

	    mRequestManager.execute(checkVocabularyListReqest, this);
	    mRequestList.add(checkVocabularyListReqest);
	}

    }

    private void configureVocabularyBoard(String id) {
	mRefreshActionItem.showProgress(true);
	Request configureVocabularyBoardRequest = VelloRequestFactory
		.configureVocabularyBoardRequest(id);
	mRequestManager.execute(configureVocabularyBoardRequest, this);
	mRequestList.add(configureVocabularyBoardRequest);
    }

    private void getDueWordCardList() {
	mRefreshActionItem.showProgress(true);
	Request getDueWordCardListRequest = VelloRequestFactory
		.getDueWordCardListRequest(VelloRequestFactory.REQUEST_TYPE_GET_DUE_WORDCARD_LIST);
	mRequestManager.execute(getDueWordCardListRequest, this);
	mRequestList.add(getDueWordCardListRequest);
    }

    @Override
    public void connectionErrorDialogCancel(Request request) {
    }

    @Override
    public void connectionErrorDialogRetry(Request request) {
    }

    @Override
    public void onDismiss(ListView listView, int[] reverseSortedPositions) {
	for (int position : reverseSortedPositions) {
	    mGoogleCardsAdapter.remove(position);
	}
    }

    @Override
    public void onRefreshButtonClick(RefreshActionItem sender) {
	mGoogleCardsAdapter.clear();
	getDueWordCardList();
    }
    
    private class WordCardToWordTask extends AsyncTask<WordCard, Integer, ArrayList<Word>> {

	@Override
	protected ArrayList<Word> doInBackground(WordCard... wordcards) {
	    
	    ArrayList<Word> wordList = new ArrayList<Word>();
	    for (WordCard wordcard : wordcards) {
		IcibaWord word = new IcibaWord();
		String xmlWord = wordcard.desc;
		word = new IcibaWordXmlParser().parse(xmlWord);
		
		if (word == null) {
		    word = new IcibaWord();
		}
		word.idCard = wordcard.id;
		word.keyword = wordcard.name;
		wordList.add(word);
	    }
	    return wordList;
	}

	@Override
	protected void onPostExecute(ArrayList<Word> result) {
		mGoogleCardsAdapter.addAll(result);
		mRefreshActionItem.showBadge(String.valueOf(mGoogleCardsAdapter.getCount()));
		mRefreshActionItem.showProgress(false);
	}
    }
}
