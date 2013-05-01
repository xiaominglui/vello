package com.mili.xiaominglui.app.vello.ui;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.requestmanager.RequestManager.RequestListener;
import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.data.model.Board;
import com.mili.xiaominglui.app.vello.data.model.List;
import com.mili.xiaominglui.app.vello.data.model.WordCard;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;
import com.mili.xiaominglui.app.vello.dialogs.ConnectionErrorDialogFragment.ConnectionErrorDialogListener;
import com.mili.xiaominglui.app.vello.util.AccountUtils;

import java.util.ArrayList;

public class MainActivity extends BaseActivity implements RequestListener,
	ConnectionErrorDialogListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private Menu mOptionsMenu;
    private Context mContext;

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
    }

    @Override
    protected void onResume() {
	super.onResume();
	for (int i = 0; i < mRequestList.size(); i++) {
	    Request request = mRequestList.get(i);

	    if (mRequestManager.isRequestInProgress(request)) {
		mRequestManager.addRequestListener(this, request);
		setProgressBarIndeterminateVisibility(true);
	    } else {
		mRequestManager.callListenerWithCachedData(this, request);
		i--;
		mRequestList.remove(request);
	    }
	}
	getDueWordCardList();
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
	    setProgressBarIndeterminateVisibility(false);
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
		    String id = resultData.getString(VelloRequestFactory.BUNDLE_EXTRA_VOCABULARY_BOARD_ID);
		    
		    configureVocabularyBoard(id);
		}
		return;
		
	    case VelloRequestFactory.REQUEST_TYPE_GET_DUE_WORDCARD_LIST:
		ArrayList<WordCard> wordCardList = resultData
		.getParcelableArrayList(VelloRequestFactory.BUNDLE_EXTRA_WORDCARD_LIST);
		
		for (WordCard wc : wordCardList) {
		    System.out.println(wc.name);
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
	    setProgressBarIndeterminateVisibility(false);
	    mRequestList.remove(request);

	    showBadDataErrorDialog();
	}
    }

    @Override
    public void onRequestCustomError(Request request, Bundle resultData) {
	// Never called.
    }

    private void checkVocabularyBoard() {
	setProgressBarIndeterminateVisibility(true);
	Request checkVocabularyBoardRequest = VelloRequestFactory
		.checkVocabularyBoardRequest();
	mRequestManager.execute(checkVocabularyBoardRequest, this);
	mRequestList.add(checkVocabularyBoardRequest);
    }

    private void createVocabularyBoard() {
	setProgressBarIndeterminateVisibility(true);
	Request createVocabularyBoardRequest = VelloRequestFactory.createVocabularyBoardRequest();
	mRequestManager.execute(createVocabularyBoardRequest, this);
	mRequestList.add(createVocabularyBoardRequest);
    }

    private void createVocabularyList(int position) {
	setProgressBarIndeterminateVisibility(true);
	Request createVocabularyListRequest = VelloRequestFactory
		.createVocabularyListRequest(position);
	mRequestManager.execute(createVocabularyListRequest, this);
	mRequestList.add(createVocabularyListRequest);
    }

    private void reOpenVocabulayList(int position, String id) {
	setProgressBarIndeterminateVisibility(true);
	Request reOpenVocabularyListRequest = VelloRequestFactory
		.reOpenVocabularyListRequest(position, id);
	mRequestManager.execute(reOpenVocabularyListRequest, this);
	mRequestList.add(reOpenVocabularyListRequest);
    }

    private void checkVocabularyLists() {
	setProgressBarIndeterminateVisibility(true);
	for (int i = 0; i < AccountUtils.VOCABULARY_LISTS_TITLE_ID.length; i++) {
	    Request checkVocabularyListReqest = VelloRequestFactory
		    .checkVocabularyListRequest(i);

	    mRequestManager.execute(checkVocabularyListReqest, this);
	    mRequestList.add(checkVocabularyListReqest);
	}

    }

    private void configureVocabularyBoard(String id) {
	setProgressBarIndeterminateVisibility(true);
	Request configureVocabularyBoardRequest = VelloRequestFactory
		.configureVocabularyBoardRequest(id);
	mRequestManager.execute(configureVocabularyBoardRequest, this);
	mRequestList.add(configureVocabularyBoardRequest);
    }
    
    private void getDueWordCardList() {
	setProgressBarIndeterminateVisibility(true);
	Request getDueWordCardListRequest = VelloRequestFactory.getDueWordCardListRequest(VelloRequestFactory.REQUEST_TYPE_GET_DUE_WORDCARD_LIST);
	mRequestManager.execute(getDueWordCardListRequest, this);
	mRequestList.add(getDueWordCardListRequest);
    }

    @Override
    public void connectionErrorDialogCancel(Request request) {
    }

    @Override
    public void connectionErrorDialogRetry(Request request) {
    }
}
