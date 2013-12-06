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
import android.widget.Toast;

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
	
	private CardListView mCardList;
	private ArrayList<Card> mCards;
	private CardArrayAdapter mCardArrayAdapter;
	
	private onStatusChangedListener mListener;

	private String mCurFilter = "";
	private boolean mIsSearching = false;
	
	private ActionBarHelper mActionBar;
	
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
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_home, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mCards = new ArrayList<Card>();
		mCardList = (CardListView) getActivity().findViewById(R.id.card_list);
		getLoaderManager().initLoader(0, null, this);
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
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
		    criteria.addLike(DbDictCard.Columns.KEYWORD, mCurFilter + "%");
		    return new CursorLoader(getActivity(), DbDictCard.CONTENT_URI, DbDictCard.PROJECTION, criteria.getWhereClause(), criteria.getWhereParams(), criteria.getOrderClause());
		}
		
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mCards.clear();
		
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
				rc.setId(rc.trelloCard.id);
				rc.init();
				/*
				rc.setOnSwipeListener(new Card.OnSwipeListener() {

					@Override
					public void onSwipe(Card card) {
						Log.d("mingo.lv", "onSwipe");
						asyncMarkRecalledWord(card);

					}
				});

				rc.setOnUndoSwipeListListener(new Card.OnUndoSwipeListListener() {

					@Override
					public void onUndoSwipe(Card card) {
						Log.d("mingo.lv", "onUndoSwipe");
						asyncUnmarkRecalledWord(card);
					}
				});

				rc.setOnLongClickListener(new Card.OnLongCardClickListener() {

					@Override
					public boolean onLongClick(Card card, View view) {
						// TODO Auto-generated method stub
						Toast.makeText(getSherlockActivity().getApplicationContext(), "onLongClick", Toast.LENGTH_SHORT).show();
						return false;
					}
				});
				*/
				mCards.add(rc);
				mCardArrayAdapter = new CardArrayAdapter(getActivity(),mCards);
				//Enable undo controller!
		        mCardArrayAdapter.setEnableUndo(true);
		        
		        if (mCardList != null) {
		        	mCardList.setAdapter(mCardArrayAdapter);
		        }
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loder) {
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

	@Override
	public void onClick(DialogInterface arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onLongClick(View arg0) {
		// TODO Auto-generated method stub
		return false;
	}
}
