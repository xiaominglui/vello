
package com.mili.xiaominglui.app.vello.ui;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

import android.accounts.Account;
import android.app.ActionBar;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.BaseColumns;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.ActionMode.Callback;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
import com.actionbarsherlock.widget.SearchView.OnSuggestionListener;
import com.android.deskclock.widget.ActionableToastBar;
import com.android.deskclock.widget.swipeablelistview.SwipeableListView;
import com.atermenji.android.iconictextview.IconicTextView;
import com.atermenji.android.iconictextview.icon.FontAwesomeIcon;
import com.devspark.appmsg.AppMsg;
import com.haarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.haarman.listviewanimations.itemmanipulation.SwipeDismissAdapter;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
import com.manuelpeinado.refreshactionitem.ProgressIndicatorType;
import com.manuelpeinado.refreshactionitem.RefreshActionItem;
import com.manuelpeinado.refreshactionitem.RefreshActionItem.RefreshActionListener;
import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.authenticator.Constants;
import com.mili.xiaominglui.app.vello.config.VelloConfig;
import com.mili.xiaominglui.app.vello.data.factory.IcibaWordXmlParser;
import com.mili.xiaominglui.app.vello.data.model.Definition;
import com.mili.xiaominglui.app.vello.data.model.Definitions;
import com.mili.xiaominglui.app.vello.data.model.IcibaWord;
import com.mili.xiaominglui.app.vello.data.model.Phonetics;
import com.mili.xiaominglui.app.vello.data.model.Phoneticss;
import com.mili.xiaominglui.app.vello.data.model.Word;
import com.mili.xiaominglui.app.vello.data.model.WordCard;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbWordCard;
import com.mili.xiaominglui.app.vello.data.provider.VelloProvider;
import com.mili.xiaominglui.app.vello.data.provider.util.ProviderCriteria;
import com.mili.xiaominglui.app.vello.service.VelloService;
import com.mili.xiaominglui.app.vello.ui.MainActivity.WordCardAdapter.ItemHolder;
import com.mili.xiaominglui.app.vello.util.AccountUtils;
import com.tjerkw.slideexpandable.library.SlideExpandableListAdapter;

public class MainActivity extends BaseActivity implements RefreshActionListener,
        OnQueryTextListener, OnSuggestionListener, LoaderCallbacks<Cursor>, Callback {
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
                case VelloService.MSG_SPINNER_ON:
                    theActivity.mRefreshActionItem.showProgress(true);
                    break;
                case VelloService.MSG_SPINNER_OFF:
                    theActivity.mRefreshActionItem.showProgress(false);
                    break;
                case VelloService.MSG_DIALOG_BAD_DATA_ERROR_SHOW:
                    theActivity.showBadDataErrorDialog();
                    break;
                case VelloService.MSG_DIALOG_CONNECTION_ERROR_SHOW:
                    // TODO
//                     ConnectionErrorDialogFragment.show();
                    break;
                case VelloService.MSG_TOAST_INIT_VOCABULARY_START:
                    AppMsg.makeText(theActivity.mActivity, R.string.toast_init_vocabulary_start,
                            AppMsg.STYLE_INFO).setLayoutGravity(Gravity.BOTTOM).show();
                    break;
                case VelloService.MSG_TOAST_GET_DUE_WORD:
                    AppMsg.makeText(theActivity.mActivity, R.string.toast_get_due_word, AppMsg.STYLE_INFO)
                            .setLayoutGravity(Gravity.BOTTOM).show();
                    break;
                case VelloService.MSG_TOAST_INIT_VOCABULARY_END:
                    AppMsg.makeText(theActivity.mActivity,
                            R.string.toast_init_vocabulary_end,
                            AppMsg.STYLE_INFO)
                            .setLayoutGravity(Gravity.BOTTOM)
                            .show();
                    break;
                case VelloService.MSG_TOAST_NO_WORD_NOW:
                    AppMsg.makeText(theActivity.mActivity, R.string.toast_no_word_now,
                            AppMsg.STYLE_CONFIRM).setLayoutGravity(Gravity.TOP)
                            .show();
                    break;
                case VelloService.MSG_TOAST_NOT_AVAILABLE_WORD:
                    AppMsg.makeText(theActivity.mActivity, R.string.toast_not_available_word,
                            AppMsg.STYLE_ALERT).setLayoutGravity(Gravity.TOP)
                            .show();
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
            Toast.makeText(getApplicationContext(), R.string.local_service_connected,
                    Toast.LENGTH_SHORT).show();

            try {
                Message msg = Message.obtain(null, VelloService.MSG_REGISTER_CLIENT);
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
            Toast.makeText(getApplicationContext(), R.string.local_service_disconnected,
                    Toast.LENGTH_SHORT).show();
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
        bindService(new Intent(this,
                VelloService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    private GoogleCardsCursorAdapter mGoogleCardsAdapter;
    private RefreshActionItem mRefreshActionItem;

    private SuggestionsAdapter mSuggestionsAdapter;
    private static final String[] COLUMNS = {
            BaseColumns._ID,
            SearchManager.SUGGEST_COLUMN_TEXT_1,
    };

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
                    cv.put(DbWordCard.Columns.SYNCINNEXT.getName(), "true");

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
                    
                    Date now = new Date(rightNowUnixTime);
                    String stringNow = format.format(now);
                    cv.put(DbWordCard.Columns.DATE_LAST_ACTIVITY.getName(), stringNow);

                    String newIdList = AccountUtils.getVocabularyListId(
                            mContext, positionList + 1);
                    cv.put(DbWordCard.Columns.ID_LIST.getName(), newIdList);
                    cv.put(DbWordCard.Columns.SYNCINNEXT.getName(), "true");
                }
                Uri uri = ContentUris
                        .withAppendedId(DbWordCard.CONTENT_URI, id);
                getContentResolver().update(uri, cv, null, null);
            }
        }
    }
    
    private WordCard mSelectedWordCard;
    
    // Saved status for undo
    private WordCard mDeletedWord;
    private boolean mUndoShowing = false;
    private SwipeableListView mWordsList;
    private WordCardAdapter mAdapter;
    private ActionableToastBar mUndoBar;
    private ActionMode mActionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        mActivity = this;
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        if (isFinishing()) {
            return;
        }

        initialize(savedInstanceState);
        updateLayout();
        
        
        // mGoogleCardsAdapter = new GoogleCardsCursorAdapter(this);
//        mGoogleCardsAdapter = new GoogleCardsCursorAdapter(this);
//        SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(
//                new SwipeDismissAdapter(mGoogleCardsAdapter,
//                        new MyOnDismissCallback(mGoogleCardsAdapter)));
//        swingBottomInAnimationAdapter.setListView(listView);
//
//        SlideExpandableListAdapter slideExpandableListAdapter = new SlideExpandableListAdapter(
//                swingBottomInAnimationAdapter, R.id.expandable_toggle_button,
//                R.id.expandable);
//
//        listView.setAdapter(slideExpandableListAdapter);

        getSupportLoaderManager().initLoader(0, null, this);
        mInflater = getLayoutInflater();
        doBindService();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // for (int i = 0; i < mRequestList.size(); i++) {
        // Request request = mRequestList.get(i);
        //
        // if (mRequestManager.isRequestInProgress(request)) {
        // mRequestManager.addRequestListener(this, request);
        // } else {
        // mRequestManager.callListenerWithCachedData(this, request);
        // i--;
        // mRequestList.remove(request);
        // }
        // }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // if (!mRequestList.isEmpty()) {
        // mRequestManager.removeRequestListener(this);
        // }
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
    	mAdapter = new WordCardAdapter(mContext, expandedIds, selectedWords, mWordsList);
    	mWordsList.setAdapter(mAdapter);
    	mWordsList.setVerticalScrollBarEnabled(true);
    	mWordsList.enableSwipe(true);
    	mWordsList.setOnCreateContextMenuListener(this);
    	mWordsList.setOnItemSwipeListener(new SwipeableListView.OnItemSwipeListener() {
			
			@Override
			public void onSwipe(View view) {
				final WordCardAdapter.ItemHolder itemHolder = (ItemHolder) view.getTag();
				mAdapter.removeSelectedId(itemHolder.wordcard.id);
				
			}
		});
    	
    	mWordsList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                hideUndoBar(true, event);
                return false;
            }
        });

        mUndoBar = (ActionableToastBar) findViewById(R.id.undo_bar);

        if (mUndoShowing) {
            mUndoBar.show(new ActionableToastBar.ActionClickedListener() {
                @Override
                public void onActionClicked() {
                    asyncAddWord(mDeletedWord);
                    mDeletedWord = null;
                    mUndoShowing = false;
                }
            }, 0, getResources().getString(R.string.alarm_deleted), true, R.string.alarm_undo,
                    true);
        }
    	
    	// Show action mode if needed
        int selectedNum = mAdapter.getSelectedItemsNum();
        if (selectedNum > 0) {
            mActionMode = startActionMode(this);
            setActionModeTitle(selectedNum);
        }
    }
    
    protected void asyncAddWord(final WordCard wordcard) {
		// TODO Auto-generated method stub
		
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
        // TODO
//        outState.putIntArray(KEY_EXPANDED_IDS, mAdapter.getExpandedArray());
        outState.putParcelable(KEY_DELETED_WORD, mDeletedWord);
        outState.putBoolean(KEY_UNDO_SHOWING, mUndoShowing);
    }
    
    private void updateLayout() {
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
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
    public void onRefreshButtonClick(RefreshActionItem sender) {
//        sendMessageToService(VelloService.MSG_GET_DUE_WORDCARD_LIST);
    	triggerRefresh();
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
            // TODO
            // lookUpWord(query.trim().toLowerCase());
        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
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
        criteria.addNe(DbWordCard.Columns.SYNCINNEXT, "true");

        return new CursorLoader(this, DbWordCard.CONTENT_URI,
                DbWordCard.PROJECTION, criteria.getWhereClause(),
                criteria.getWhereParams(), criteria.getOrderClause());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//        mGoogleCardsAdapter.changeCursor(data);
        mAdapter.swapCursor(data);
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
    
    /***
     * Activate/update/close action mode according to the number of selected views.
     */
    private void updateActionMode() {
        int selectedNum = mAdapter.getSelectedItemsNum();
        if (mActionMode == null && selectedNum > 0) {
            // Start the action mode
            mActionMode = startActionMode(this);
            setActionModeTitle(selectedNum);
        } else if (mActionMode != null) {
            if (selectedNum > 0) {
                // Update the number of selected items in the title
                setActionModeTitle(selectedNum);
            } else {
                // No selected items. close the action mode
                mActionMode.finish();
                mActionMode = null;
            }
        }
    }
    
    /***
     * Display the number of selected items on the action bar in action mode
     * @param items - number of selected items
     */
    private void setActionModeTitle(int items) {
        mActionMode.setTitle(String.format(getString(R.string.alarms_selected), items));
    }

    
    public class WordCardAdapter extends CursorAdapter {
    	private final Context mContext;
    	private final LayoutInflater mFactory;
    	private final int mBackgroundColorSelected;
        private final int mBackgroundColor;
    	private final ListView mList;
    	
    	private final HashSet<Integer> mExpanded = new HashSet<Integer>();
    	private final HashSet<Integer> mSelectedWords = new HashSet<Integer>();
    	
    	public class ItemHolder {
    		// views for optimization
    		LinearLayout alarmItem;
            IconicTextView iconicLifeCount;
            TextView textViewLifeCount;
            TextView textViewKeyword;
            
            View expandArea;

            LinearLayout linearLayoutPhoneticArea;
            LinearLayout linearLayoutDefinitionArea;
            ViewGroup collapse;
            
            View hairLine;
            
    		// Other states
            WordCard wordcard;
            IcibaWord word;
            String desc;
            String idList;
            Phoneticss p;
            Definitions d;
    	}
    	
    	// Used for scrolling an expanded item in the list to make sure it is fully visible.
    	private int mScrollWordId = -1;
    	private final Runnable mScrollRunnable = new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (mScrollWordId != -1) {
					View v = getViewById(mScrollWordId);
                    if (v != null) {
                        Rect rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                        mList.requestChildRectangleOnScreen(v, rect, false);
                    }
                    mScrollWordId = -1;
				}
			}
    	};
    	
    	public WordCardAdapter(Context context, int[] expandedIds, int[] selectedWords, ListView list) {
    		super(context, null, 0);
    		mContext = context;
    		mFactory = LayoutInflater.from(context);
    		mList = list;
    		
    		Resources res = mContext.getResources();
    		
    		mBackgroundColorSelected = res.getColor(R.color.alarm_selected_color);
            mBackgroundColor = res.getColor(R.color.alarm_whiteish);
    		
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
                // May happen if the last word was deleted and the cursor refreshed while the
                // list is updated.
                Log.v(TAG, "couldn't move cursor to position " + position);
                return null;
            }
            View v;
            if (convertView == null) {
                v = newView(mContext, getCursor(), parent);
            } else {
                // Do a translation check to test for animation. Change this to something more
                // reliable and robust in the future.
                if (convertView.getTranslationX() != 0 || convertView.getTranslationY() != 0) {
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
			// TODO Auto-generated method stub
		    final WordCard wordcard = new WordCard(cursor);
		    final ItemHolder itemHolder = (ItemHolder) view.getTag();
		    itemHolder.wordcard = wordcard;
		    itemHolder.desc = itemHolder.wordcard.desc;
		    itemHolder.word = IcibaWordXmlParser.parse(itemHolder.desc);
		    
			if (mSelectedWords.contains(itemHolder.wordcard.id)) {
			    itemHolder.alarmItem.setBackgroundColor(mBackgroundColorSelected);
                itemHolder.alarmItem.setAlpha(1f);
			} else {
			    itemHolder.alarmItem.setBackgroundColor(mBackgroundColor);
			}
			
            itemHolder.iconicLifeCount.setIcon(FontAwesomeIcon.HEART);
            itemHolder.iconicLifeCount.setTextColor(Color.GRAY);
            itemHolder.idList = itemHolder.wordcard.idList;
            int positionList = AccountUtils.getVocabularyListPosition(mContext,
            		itemHolder.idList);
            String lifeString = "N";
            if (positionList != 0) {
                lifeString = String.valueOf(8 - positionList + 1);
            }
            itemHolder.textViewLifeCount.setText(lifeString);
            itemHolder.textViewLifeCount.setTextColor(Color.GRAY);

            itemHolder.textViewKeyword.setText(itemHolder.word.keyword);

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

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			final View view = mFactory.inflate(R.layout.alarm_time, parent, false);
			
			// standard view holder optimization
            final ItemHolder holder = new ItemHolder();
            holder.alarmItem = (LinearLayout) view.findViewById(R.id.alarm_item);
            holder.iconicLifeCount = (IconicTextView) view.findViewById(R.id.life_sign);
            holder.textViewLifeCount = (TextView) view.findViewById(R.id.life_count);
            holder.textViewKeyword = (TextView) view.findViewById(R.id.keyword);
            holder.expandArea = view.findViewById(R.id.expand_area);
            holder.linearLayoutPhoneticArea = (LinearLayout) view.findViewById(R.id.phonetics_area);
            holder.linearLayoutDefinitionArea = (LinearLayout) view.findViewById(R.id.definition_area);
            holder.hairLine = view.findViewById(R.id.hairline);
            holder.collapse = (ViewGroup) view.findViewById(R.id.collapse);
            
            view.setTag(holder);
			return view;
		}
		
		public void removeSelectedId(int id) {
			mSelectedWords.remove(id);
		}
		
		private View getViewById(int id) {
            for (int i = 0; i < mList.getCount(); i++) {
                View v = mList.getChildAt(i);
                if (v != null) {
                    ItemHolder h = (ItemHolder)(v.getTag());
                    if (h != null && h.wordcard.id == id) {
                        return v;
                    }
                }
            }
            return null;
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
    
    private void triggerRefresh() {
        Bundle extras = new Bundle();
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(
                new Account(AccountUtils.getChosenAccountName(this), Constants.ACCOUNT_TYPE), VelloProvider.AUTHORITY, extras);
    }

	@Override
	public boolean onCreateActionMode(
			com.actionbarsherlock.view.ActionMode mode, Menu menu) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onPrepareActionMode(
			com.actionbarsherlock.view.ActionMode mode, Menu menu) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onActionItemClicked(
			com.actionbarsherlock.view.ActionMode mode, MenuItem item) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onDestroyActionMode(com.actionbarsherlock.view.ActionMode mode) {
		// TODO Auto-generated method stub
		
	}
}
