package com.mili.xiaominglui.app.vello.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.requestmanager.RequestManager.RequestListener;
import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.data.model.Board;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;
import com.mili.xiaominglui.app.vello.dialogs.ConnectionErrorDialogFragment.ConnectionErrorDialogListener;
import com.mili.xiaominglui.app.vello.util.AccountUtils;

import java.util.ArrayList;

public class MainActivity extends BaseActivity implements RequestListener, ConnectionErrorDialogListener {
    private Menu mOptionsMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

	if (isFinishing()) {
	    return;
	}

	if (!AccountUtils.isInitialized(this)) {
	    // has no initialized
	    initializeTrelloAccount();

	}
	setContentView(R.layout.activity_main);
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
	    
	    ArrayList<Board> boardList = resultData.getParcelableArrayList(VelloRequestFactory.BUNDLE_EXTRA_TRELLO_BOARD_LIST);
	    for (Board board : boardList) {
		Log.d("mingo.lv", board.name + "/" + board.desc);
	    }
	}
    }

    @Override
    public void onRequestConnectionError(Request request, int statusCode) {
	// TODO Auto-generated method stub

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

    public void initializeTrelloAccount() {
	setProgressBarIndeterminateVisibility(true);
	Request queryBoardRequest = VelloRequestFactory
		.queryTrelloBoardRequest();
	mRequestManager.execute(queryBoardRequest, this);
	mRequestList.add(queryBoardRequest);
    }

    @Override
    public void connectionErrorDialogCancel(Request request) {
    }

    @Override
    public void connectionErrorDialogRetry(Request request) {
    }
}
