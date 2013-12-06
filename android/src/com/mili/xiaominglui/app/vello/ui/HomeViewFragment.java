package com.mili.xiaominglui.app.vello.ui;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.ActionMode.Callback;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.atermenji.android.iconictextview.IconicTextView;
import com.atermenji.android.iconictextview.icon.FontAwesomeIcon;
import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.adapter.HomeCardArrayAdapter;
import com.mili.xiaominglui.app.vello.config.VelloConfig;
import com.mili.xiaominglui.app.vello.data.factory.MiliDictionaryJsonParser;
import com.mili.xiaominglui.app.vello.data.model.Definition;
import com.mili.xiaominglui.app.vello.data.model.Definitions;
import com.mili.xiaominglui.app.vello.data.model.IcibaWord;
import com.mili.xiaominglui.app.vello.data.model.Phonetics;
import com.mili.xiaominglui.app.vello.data.model.Phoneticss;
import com.mili.xiaominglui.app.vello.data.model.TrelloCard;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbDictCard;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbWordCard;
import com.mili.xiaominglui.app.vello.data.provider.util.ProviderCriteria;
import com.mili.xiaominglui.app.vello.util.AccountUtils;

public class HomeViewFragment extends SherlockFragment implements LoaderManager.LoaderCallbacks<Cursor>, OnLongClickListener, Callback, DialogInterface.OnClickListener {
	private static final String TAG = HomeViewFragment.class.getSimpleName();
	
	private TrelloCard mDeletedWord;
	private boolean mUndoShowing = false;
	private boolean mInDeleteConfirmation = false;

	private CardListView mCardList;
	private ArrayList<Card> mCards;
	private CardArrayAdapter mCardArrayAdapter;
	
//	private WordCardAdapter mAdapter;
	private ViewGroup mRootView;
	private onStatusChangedListener mListener;

	private String mCurFilter = "";
	private boolean mIsSearching = false;
	
	private ActionBarHelper mActionBar;
	private ActionMode mActionMode;
	
	public static Fragment newInstance() {
		Fragment f = new HomeViewFragment();
		return f;
	}
	
//	private SwipeableListView.OnItemSwipeListener mReviewSwipeListener = new SwipeableListView.OnItemSwipeListener() {
//        
//        @Override
//        public void onSwipe(View view) {
//            final WordCardAdapter.ItemHolder itemHolder = (WordCardAdapter.ItemHolder) view.getTag();
//            // if wordcard expanded, do NOT mark reviewed plus
//            if (!mAdapter.isWordExpanded(itemHolder.wordcard)) {
//            	updateActionMode();
//                asyncMarkRecalledWord(itemHolder.wordcard);
//                mListener.onWordReviewed();
//            } else {
//                // review failed
//                asyncDeleteWordCache(itemHolder.wordcard);
//            }
//        }
//    };
    
	public interface onStatusChangedListener {
		public void onModeChanged(int modeColor);
		public void onAllReviewed();
		public void onWordReviewed();
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (onStatusChangedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement onStatusChangedListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
//		outState.putParcelable(KEY_DELETED_WORD, mDeletedWord);
//		outState.putBoolean(KEY_UNDO_SHOWING, mUndoShowing);
//		outState.putIntArray(KEY_SELECTED_WORD_CARDS, mAdapter.getSelectedWordCardsArray());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
//		int[] selectedWordCards = null;
		
//		if (savedInstanceState != null) {
//			mDeletedWord = savedInstanceState.getParcelable(KEY_DELETED_WORD);
//			mUndoShowing = savedInstanceState.getBoolean(KEY_UNDO_SHOWING);
//			selectedWordCards = savedInstanceState.getIntArray(KEY_SELECTED_WORD_CARDS);
//		}
		
		mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_home, null);
		
		mCards = new ArrayList<Card>();
		mCardArrayAdapter = new HomeCardArrayAdapter(getActivity(), mCards);
		mCardList = (CardListView) mRootView.findViewById(R.id.card_list);
		if (mCardList != null) {
			mCardList.setAdapter(mCardArrayAdapter);
		}
		
		/*
		mAdapter = new WordCardAdapter(getActivity(), null, selectedWordCards, mCardList);
		mAdapter.setLongClickListener(this);
		mCardList.setAdapter(mAdapter);
		mCardList.setVerticalScrollBarEnabled(true);
		mCardList.setOnCreateContextMenuListener(this);

		mCardList.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
//				hideUndoBar(true, event);
				return false;
			}
		});
		
		*/


//		if (mUndoShowing) {
//			mUndoBar.show(new ActionableToastBar.ActionClickedListener() {
//				@Override
//				public void onActionClicked() {
//					asyncUnmarkDeleteWord(mDeletedWord);
//					mDeletedWord = null;
//					mUndoShowing = false;
//				}
//			}, 0, getResources().getString(R.string.word_reviewed), true,
//					R.string.word_reviewed_undo, true);
//		}
		
		mActionBar = createActionBarHelper();
		mActionBar.init();
		
		// Show action mode if needed
//        int selectedNum = mAdapter.getSelectedItemsNum();
//        if (selectedNum > 0) {
//            mActionMode = getSherlockActivity().startActionMode(this);
//            setActionModeTitle(selectedNum);
//        }
        
		return mRootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(0, null, this);
		
	}
	
	@Override
	public void onDestroyView() {
//		mAdapter.swapCursor(null);
		super.onDestroyView();
	}
	
	/***
     * Display the number of selected items on the action bar in action mode
     * @param items - number of selected items
     */
    private void setActionModeTitle(int items) {
        mActionMode.setTitle(String.format(getString(R.string.word_cards_selected), items));
    }
	
	@SuppressLint("SimpleDateFormat")
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// show all open WordCards whose due time bigger than mobile local now
		// time && syncInNext mark not set
		ProviderCriteria criteria = new ProviderCriteria();
		if (TextUtils.isEmpty(mCurFilter)) {
			// Review Mode
		    mIsSearching = false;
		    mListener.onModeChanged(VelloConfig.REVIEW_MODE_ACTION_BAR_COLOR);
			criteria.addSortOrder(DbWordCard.Columns.DUE, true);
			Calendar rightNow = Calendar.getInstance();

			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			long rightNowUnixTime = rightNow.getTimeInMillis();
			long rightNowUnixTimeGMT = rightNowUnixTime - TimeZone.getDefault().getRawOffset();
			String now = format.format(new Date(rightNowUnixTimeGMT));
			criteria.addEq(DbWordCard.Columns.MARKDELETED, "false");
			criteria.addLt(DbWordCard.Columns.DUE, now, true);
			return new CursorLoader(getActivity(), DbWordCard.CONTENT_URI, DbWordCard.PROJECTION, criteria.getWhereClause(), criteria.getWhereParams(), criteria.getOrderClause());
		} else {
			// Dictionary Mode
		    mIsSearching = true;
		    mListener.onModeChanged(VelloConfig.DICTIONARY_MODE_ACTION_BAR_COLOR);
//		    mCardList.enableSwipe(false);
//		    mCardList.setOnItemSwipeListener(null);
		    criteria.addLike(DbDictCard.Columns.KEYWORD, mCurFilter + "%");
		    return new CursorLoader(getActivity(), DbDictCard.CONTENT_URI, DbDictCard.PROJECTION, criteria.getWhereClause(), criteria.getWhereParams(), criteria.getOrderClause());
		}
		
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mCardArrayAdapter.clear();
		if (mIsSearching) {
			// in Dictionary Mode
			if (data == null) return;
			for (data.moveToFirst(); !data.isAfterLast(); data.moveToNext()) {
				DictCard dc = new DictCard(getActivity().getApplicationContext(), data);
				dc.setSwipeable(false);
				mCardArrayAdapter.add(dc);
			}
		} else {
			// in Review Mode
			for (data.moveToFirst(); !data.isAfterLast(); data.moveToNext()) {
				ReviewCard rc = new ReviewCard(getActivity().getApplicationContext(), data);
				rc.setSwipeable(true);
				rc.setId(rc.trelloCard.id);
				mCardArrayAdapter.setEnableUndo(true);
				mCardArrayAdapter.add(rc);
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loder) {
//		mAdapter.swapCursor(null);
	}
	
	
	private void asyncMarkDeleteWordRemotely(final Integer [] wordIds) {
		final AsyncTask<Integer, Void, Void> deleteTask = new AsyncTask<Integer, Void, Void>() {
            @Override
            protected Void doInBackground(Integer... ids) {
            	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                for (final int id : ids) {
					Calendar rightNow = Calendar.getInstance();
					long rightNowUnixTime = rightNow.getTimeInMillis();
                	Date rightNowDate = new Date(rightNowUnixTime);
					String stringRightNow = format.format(rightNowDate);
                	ContentValues cv = new ContentValues();
                	cv.put(DbWordCard.Columns.MARKDELETED.getName(), "true");
                	cv.put(DbWordCard.Columns.DATE_LAST_OPERATION.getName(), stringRightNow);
                	Uri uri = ContentUris.withAppendedId(DbWordCard.CONTENT_URI, id);
					getActivity().getContentResolver().update(uri, cv, null, null);
                }
                return null;
            }
        };
        deleteTask.execute(wordIds);
	}

//	private void hideUndoBar(boolean animate, MotionEvent event) {
//		if (mUndoBar != null) {
//			if (event != null && mUndoBar.isEventInToastBar(event)) {
//				// Avoid touches inside the undo bar.
//				return;
//			}
//			mUndoBar.hide(animate);
//		}
//		mDeletedWord = null;
//		mUndoShowing = false;
//	}
	
	void onQueryTextChange(String newText) {
		mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
		getLoaderManager().restartLoader(0, null, this);
	}
	
	/**
	 * Create a compatible helper that will manipulate the action bar if
	 * available.
	 */
	private ActionBarHelper createActionBarHelper() {
		return new ActionBarHelper();
	}
	
	private class ActionBarHelper {
		private final ActionBar mActionBar;

		private ActionBarHelper() {
			mActionBar = ((SherlockFragmentActivity) getActivity()).getSupportActionBar();
		}

		public void init() {
			mActionBar.setDisplayHomeAsUpEnabled(false);
			mActionBar.setHomeButtonEnabled(false);
		}

	}
	
	/***
     * Handle the delete word cards confirmation dialog
     */

//    private void showConfirmationDialog() {
//        Resources res = getResources();
//        String msg = String.format(res.getQuantityText(R.plurals.word_card_delete_confirmation, mAdapter.getSelectedItemsNum()).toString());
//        
//        DialogFragment frag = WordsDeleteConfirmationDialog.newInstance(msg);
//        frag.setTargetFragment(this, 0);
//        frag.show(getFragmentManager(), msg);
//        mInDeleteConfirmation = true;
//    }

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == -1) {
//            if (mAdapter != null) {
//                mAdapter.deleteSelectedWordCards();
//                mActionMode.finish();
//            }
        }
        dialog.dismiss();
        mInDeleteConfirmation = false;
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
//		getActivity().getMenuInflater().inflate(R.menu.word_card_cab_menu, menu);
		MenuInflater inflater = mode.getMenuInflater();
		inflater.inflate(R.menu.word_card_cab_menu, menu);
        return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch (item.getItemId()) {
		// Delete selected items and close CAB.
		case R.id.menu_item_delete_word_card:
//			showConfirmationDialog();
			break;
		default:
			break;
		}
		return false;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
//		if(mAdapter != null) {
//            mAdapter.clearSelectedAlarms();
//        }
        mActionMode = null;
	}

	@Override
	public boolean onLongClick(View v) {
//		mAdapter.toggleSelectState(v);
//        mAdapter.notifyDataSetChanged();
        return false;
	}
}
