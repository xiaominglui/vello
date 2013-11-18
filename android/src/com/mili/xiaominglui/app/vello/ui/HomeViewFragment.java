package com.mili.xiaominglui.app.vello.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.android.deskclock.widget.swipeablelistview.SwipeableListView;
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
import com.mili.xiaominglui.app.vello.data.model.WordCard;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbDictCard;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbWordCard;
import com.mili.xiaominglui.app.vello.data.provider.util.ProviderCriteria;
import com.mili.xiaominglui.app.vello.dialogs.WordsDeleteConfirmationDialog;
import com.mili.xiaominglui.app.vello.util.AccountUtils;
import com.sherlock.navigationdrawer.compat.SherlockActionBarDrawerToggle;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.TimeZone;

public class HomeViewFragment extends SherlockFragment implements LoaderManager.LoaderCallbacks<Cursor>, OnLongClickListener, Callback, DialogInterface.OnClickListener {
	private static final String TAG = HomeViewFragment.class.getSimpleName();
	
	private static final String KEY_DELETED_WORD = "deletedWord";
	private static final String KEY_UNDO_SHOWING = "undoShowing";
	private static final String KEY_SELECTED_WORD_CARDS = "selectedWordCards";
	
	private WordCard mDeletedWord;
	private boolean mUndoShowing = false;
	private boolean mInDeleteConfirmation = false;

	private CardListView mCardList;
	private ArrayList<Card> mCards;
	private CardArrayAdapter mCardArrayAdapter;
	
	private WordCardAdapter mAdapter;
	private ViewGroup mRootView;
	private onStatusChangedListener mListener;

	private String mCurFilter = "";
	private boolean mIsSearching = false;
	
	private DrawerLayout mDrawerLayout;
	private ListView listView;
	private SherlockActionBarDrawerToggle mDrawerToggle;
	private ActionBarHelper mActionBar;
	private ActionMode mActionMode;
	
	public static Fragment newInstance() {
		Fragment f = new HomeViewFragment();
		return f;
	}
	
	private SwipeableListView.OnItemSwipeListener mReviewSwipeListener = new SwipeableListView.OnItemSwipeListener() {
        
        @Override
        public void onSwipe(View view) {
            final WordCardAdapter.ItemHolder itemHolder = (WordCardAdapter.ItemHolder) view.getTag();
            // if wordcard expanded, do NOT mark reviewed plus
            if (!mAdapter.isWordExpanded(itemHolder.wordcard)) {
            	updateActionMode();
                asyncMarkRecalledWord(itemHolder.wordcard);
                mListener.onWordReviewed();
            } else {
                // review failed
                asyncDeleteWordCache(itemHolder.wordcard);
            }
        }
    };
    
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
		outState.putParcelable(KEY_DELETED_WORD, mDeletedWord);
		outState.putBoolean(KEY_UNDO_SHOWING, mUndoShowing);
		outState.putIntArray(KEY_SELECTED_WORD_CARDS, mAdapter.getSelectedWordCardsArray());
	}
	
	/**
	 * This list item click listener implements very simple view switching by
	 * changing the primary content text. The drawer is closed when a selection
	 * is made.
	 */
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//			mContent.setText(Shakespeare.DIALOGUE[position]);
			mActionBar.setTitle(VelloConfig.DRAWER_MENU_TITLES[position]);
			mDrawerLayout.closeDrawer(listView);
		}
	}
	
	/**
	 * A drawer listener can be used to respond to drawer events such as
	 * becoming fully opened or closed. You should always prefer to perform
	 * expensive operations such as drastic relayout when no animation is
	 * currently in progress, either before or after the drawer animates.
	 * 
	 * When using ActionBarDrawerToggle, all DrawerLayout listener methods
	 * should be forwarded if the ActionBarDrawerToggle is not used as the
	 * DrawerLayout listener directly.
	 */
	private class DemoDrawerListener implements DrawerLayout.DrawerListener {
		@Override
		public void onDrawerOpened(View drawerView) {
			mDrawerToggle.onDrawerOpened(drawerView);
			mActionBar.onDrawerOpened();
		}

		@Override
		public void onDrawerClosed(View drawerView) {
			mDrawerToggle.onDrawerClosed(drawerView);
			mActionBar.onDrawerClosed();
		}

		@Override
		public void onDrawerSlide(View drawerView, float slideOffset) {
			mDrawerToggle.onDrawerSlide(drawerView, slideOffset);
		}

		@Override
		public void onDrawerStateChanged(int newState) {
			mDrawerToggle.onDrawerStateChanged(newState);
		}
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
		
		mDrawerLayout = (DrawerLayout) mRootView.findViewById(R.id.drawer_layout);
		listView = (ListView) mRootView.findViewById(R.id.left_drawer);
		
		mDrawerLayout.setDrawerListener(new DemoDrawerListener());
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		listView.setAdapter(new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1, VelloConfig.DRAWER_MENU_TITLES));
		listView.setOnItemClickListener(new DrawerItemClickListener());
		listView.setCacheColorHint(0);
		listView.setScrollingCacheEnabled(false);
		listView.setScrollContainer(false);
		listView.setFastScrollEnabled(true);
		listView.setSmoothScrollbarEnabled(true);
		
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
		
		// ActionBarDrawerToggle provides convenient helpers for tying together
		// the
		// prescribed interactions between a top-level sliding drawer and the
		// action bar.
		mDrawerToggle = new SherlockActionBarDrawerToggle(this.getActivity(),
				mDrawerLayout, R.drawable.ic_drawer_light,
				R.string.drawer_open, R.string.drawer_close);
		mDrawerToggle.syncState();
		
		// Show action mode if needed
//        int selectedNum = mAdapter.getSelectedItemsNum();
//        if (selectedNum > 0) {
//            mActionMode = getSherlockActivity().startActionMode(this);
//            setActionModeTitle(selectedNum);
//        }
        
		return mRootView;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		/*
		 * The action bar home/up action should open or close the drawer.
		 * mDrawerToggle will take care of this.
		 */
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(0, null, this);
		
	}
	
	@Override
	public void onDestroyView() {
		mAdapter.swapCursor(null);
		super.onDestroyView();
	}
	
	/***
     * Activate/update/close action mode according to the number of selected views.
     */
    private void updateActionMode() {
        int selectedNum = mAdapter.getSelectedItemsNum();
        if (mActionMode == null && selectedNum > 0) {
            // Start the action mode
            mActionMode = getSherlockActivity().startActionMode(this);
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
        mActionMode.setTitle(String.format(getString(R.string.word_cards_selected), items));
    }
	
	public class WordCardAdapter extends CursorAdapter {
		private final Context mContext;
		private final LayoutInflater mFactory;
		private final ListView mList;
		private OnLongClickListener mLongClickListener;

		private final HashSet<Integer> mExpanded = new HashSet<Integer>();
		private final HashSet<Integer> mSelectedWordCards = new HashSet<Integer>();
		private final int[] mWordCardBackgroundColor = { R.color.bg_color_new,
				R.color.bg_color_1st, R.color.bg_color_2nd,
				R.color.bg_color_3rd, R.color.bg_color_4th,
				R.color.bg_color_5th, R.color.bg_color_6th,
				R.color.bg_color_7th, R.color.bg_color_8th };
		private final int mBackgroundColorSelected;

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

		public WordCardAdapter(Context context, int[] expandedIds, int[] selectedWordCards, ListView list) {
			super(context, null, 0);
			mContext = context;
			mFactory = LayoutInflater.from(context);
			mList = list;
			Resources res = mContext.getResources();
			mBackgroundColorSelected = res.getColor(R.color.card_selected_color);

			if (expandedIds != null) {
				buildHashSetFromArray(expandedIds, mExpanded);
			}
			
			if (selectedWordCards != null) {
                buildHashSetFromArray(selectedWordCards, mSelectedWordCards);
            }
		}
		
		public void setLongClickListener(OnLongClickListener l) {
            mLongClickListener = l;
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
			itemHolder.word = MiliDictionaryJsonParser.parse(wordcard.desc);
			itemHolder.iconicLifeCount.setIcon(FontAwesomeIcon.CHECK);
			itemHolder.iconicLifeCount.setTextColor(Color.GRAY);
			itemHolder.iconicLifeCount.setVisibility(mIsSearching ? View.GONE : View.VISIBLE);
			itemHolder.idList = itemHolder.wordcard.idList;
			int positionList = AccountUtils.getVocabularyListPosition(mContext,
					itemHolder.idList);
			if (mIsSearching) {
				itemHolder.wordCardItem.setBackgroundResource(R.color.bg_dictionary_mode);
			} else if (mSelectedWordCards.contains(itemHolder.wordcard.idInLocalDB)){
				itemHolder.wordCardItem.setBackgroundColor(mBackgroundColorSelected);
                setItemAlpha(itemHolder, true);
			} else {
//				itemHolder.wordCardItem.setBackgroundResource(mWordCardBackgroundColor[positionList]);
				itemHolder.wordCardItem.setBackgroundResource(R.color.bg_dictionary_mode);
			}

			itemHolder.textViewLifeCount.setText(String.valueOf(positionList) + "/9");
			itemHolder.textViewLifeCount.setTextColor(Color.GRAY);
			itemHolder.textViewLifeCount.setVisibility(mIsSearching ? View.GONE : View.VISIBLE);

			itemHolder.textViewKeyword.setText(itemHolder.wordcard.name);

			itemHolder.expandArea.setVisibility(isWordExpanded(wordcard) ? View.VISIBLE : View.GONE);
			itemHolder.expandArea.setOnLongClickListener(mLongClickListener);
			itemHolder.infoArea.setVisibility(!isWordExpanded(wordcard) || mIsSearching ? View.VISIBLE : View.GONE);
			itemHolder.infoArea.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					//When action mode is on - simulate long click
                    if (doLongClick(view)) {
                        return;
                    }
					expandWord(itemHolder);
					itemHolder.wordCardItem.post(mScrollRunnable);
				}
			});
			itemHolder.infoArea.setOnLongClickListener(mLongClickListener);

			if (isWordExpanded(wordcard) && !mIsSearching) {
				expandWord(itemHolder);
			}
			view.setOnLongClickListener(mLongClickListener);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //When action mode is on - simulate long click
                    doLongClick(view);
                }
            });
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

			view.setTag(holder);
			return view;
		}
		
		// Sets the alpha of the item except the on/off switch. This gives a visual effect
        // for enabled/disabled alarm while leaving the on/off switch more visible
        private void setItemAlpha(ItemHolder holder, boolean enabled) {
            float alpha = enabled ? 1f : 0.5f;
            holder.infoArea.setAlpha(alpha);
            holder.expandArea.setAlpha(alpha);
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
							doLongClick(view);
						}
					});
			itemHolder.infoArea.setVisibility(View.GONE);

			if (!mIsSearching) {
				mExpanded.add(itemHolder.wordcard.idInLocalDB);
			}

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
				((TextView) phoneticsView.findViewById(R.id.phonetics_type)).setText(phonetics.type);
				((TextView) phoneticsView.findViewById(R.id.phonetics_symbol))
						.setText("[" + phonetics.symbol + "]");
				/*
				 * remove sound icon in word card at present
				((IconicTextView) phoneticsView.findViewById(R.id.phonetics_sound)).setIcon(FontAwesomeIcon.VOLUME_UP);
				((IconicTextView) phoneticsView.findViewById(R.id.phonetics_sound)).setTextColor(Color.GRAY);
				*/
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
		
		public int[] getSelectedWordCardsArray() {
            final int[] ids = new int[mSelectedWordCards.size()];
            int index = 0;
            for (int id : mSelectedWordCards) {
                ids[index] = id;
                index++;
            }
            return ids;
        }
		
		public int getSelectedItemsNum() {
            return mSelectedWordCards.size();
        }

        public void clearExpandedArray() {
            mExpanded.clear();
        }

		private void buildHashSetFromArray(int[] ids, HashSet<Integer> set) {
			for (int id : ids) {
				set.add(id);
			}
		}
		
		/***
         * Simulate a long click to override clicks on view when ActionMode is on
         * Returns true if handled a long click, false if not
         */
        private boolean doLongClick(View v) {
            if (mActionMode == null) {
                return false;
            }
            v = getTopParent(v);
            if (v != null) {
                toggleSelectState(v);
                notifyDataSetChanged();
                updateActionMode();
            }
            return true;
        }
		
		public void toggleSelectState(View v) {
            // long press could be on the parent view or one of its childs, so find the parent view
            v = getTopParent(v);
            if (v != null) {
                int id = ((ItemHolder)v.getTag()).wordcard.idInLocalDB;
                if (mSelectedWordCards.contains(id)) {
                    mSelectedWordCards.remove(id);
                } else {
                    mSelectedWordCards.add(id);
                }
            }
        }
		
		private View getTopParent(View v) {
            while (v != null && v.getId() != R.id.word_card_item) {
                v = (View) v.getParent();
            }
            return v;
        }
		
		public void deleteSelectedWordCards() {
            Integer ids [] = new Integer[mSelectedWordCards.size()];
            int index = 0;
            for (int id : mSelectedWordCards) {
                ids[index] = id;
                index ++;
            }
            asyncMarkDeleteWordRemotely(ids);
            clearSelectedAlarms();
        }
		
		public void clearSelectedAlarms() {
            mSelectedWordCards.clear();
            notifyDataSetChanged();
        }
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
//		    mCardList.enableSwipe(true);
//		    mCardList.setOnItemSwipeListener(mReviewSwipeListener);
			criteria.addSortOrder(DbWordCard.Columns.DUE, true);
			Calendar rightNow = Calendar.getInstance();

			SimpleDateFormat format = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
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
		    mCardList.setEmptyView(null);
		    criteria.addLike(DbWordCard.Columns.NAME, mCurFilter + "%");
		    return new CursorLoader(getActivity(), DbDictCard.CONTENT_URI, DbDictCard.PROJECTION, criteria.getWhereClause(), criteria.getWhereParams(), criteria.getOrderClause());
		}
		
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if (mIsSearching) {
			// in Dictionary Mode
			for (data.moveToFirst(); !data.isAfterLast(); data.moveToNext()) {
//				DictCard dc = new DictCard(getActivity().getApplicationContext(), data);
//				mCards.add(dc);
			}
		} else {
			// in Review Mode
			for (data.moveToFirst(); !data.isAfterLast(); data.moveToNext()) {
				ReviewCard rc = new ReviewCard(getActivity().getApplicationContext(), data);
				mCards.add(rc);
			}
		}
		mCardArrayAdapter.notifyDataSetChanged();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loder) {
//		mAdapter.swapCursor(null);
		mCards.clear();
	}
	
	private void asyncDeleteWordCache(WordCard wordcard) {
		Uri uri = ContentUris.withAppendedId(DbWordCard.CONTENT_URI, wordcard.idInLocalDB);
		if (VelloConfig.DEBUG_SWITCH) {
			Log.d(TAG, "uri---" + uri.toString() + " is to be deleted");
		}
		getActivity().getContentResolver().delete(uri, null, null);
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
	
	private void asyncUnmarkRecalledWord(final WordCard wordcard) {
		ContentValues cv = new ContentValues();
		cv.put(DbWordCard.Columns.CLOSED.getName(), wordcard.closed);
		cv.put(DbWordCard.Columns.DUE.getName(), wordcard.due);
		cv.put(DbWordCard.Columns.LIST_ID.getName(), wordcard.idList);
//		cv.putNull(DbWordCard.Columns.DATE_LAST_OPERATION.getName());
//		cv.remove(DbWordCard.Columns.DATE_LAST_OPERATION.getName());
		cv.put(DbWordCard.Columns.DATE_LAST_OPERATION.getName(), "");
		Uri uri = ContentUris.withAppendedId(DbWordCard.CONTENT_URI, wordcard.idInLocalDB);
		getActivity().getContentResolver().update(uri, cv, null, null);
	}

	@SuppressLint("SimpleDateFormat")
	private void asyncMarkRecalledWord(final WordCard wordcard) {
		final AsyncTask<WordCard, Void, Void> deleteTask = new AsyncTask<WordCard, Void, Void>() {

			@Override
			protected Void doInBackground(WordCard... wordcards) {
				for (final WordCard wordcard : wordcards) {
					ContentValues cv = new ContentValues();
					int positionList = AccountUtils.getVocabularyListPosition(
							getActivity(), wordcard.idList);
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
					Calendar rightNow = Calendar.getInstance();
					long rightNowUnixTime = rightNow.getTimeInMillis();
					
					if (positionList == VelloConfig.VOCABULARY_LIST_POSITION_8TH) {
						cv.put(DbWordCard.Columns.CLOSED.getName(), "true");
					} else {
						long rightNowUnixTimeGMT = rightNowUnixTime - TimeZone.getDefault().getRawOffset();
						long delta = VelloConfig.VOCABULARY_LIST_DUE_DELTA[positionList];
						long dueUnixTime = rightNowUnixTimeGMT + delta;

						
						Date dueDate = new Date(dueUnixTime);
						String stringDueDate = format.format(dueDate);
						cv.put(DbWordCard.Columns.DUE.getName(), stringDueDate);

						String newIdList = AccountUtils.getVocabularyListId(
								getActivity(), positionList + 1);
						cv.put(DbWordCard.Columns.LIST_ID.getName(), newIdList);
					}
					Date rightNowDate = new Date(rightNowUnixTime);
					String stringRightNow = format.format(rightNowDate);
					cv.put(DbWordCard.Columns.DATE_LAST_OPERATION.getName(), stringRightNow);
					Uri uri = ContentUris.withAppendedId(DbWordCard.CONTENT_URI, wordcard.idInLocalDB);
					getActivity().getContentResolver().update(uri, cv, null, null);
				}
				return null;
			}
		};
		mDeletedWord = wordcard;
		mUndoShowing = true;
		deleteTask.execute(wordcard);
//		mUndoBar.show(new ActionableToastBar.ActionClickedListener() {
//			@Override
//			public void onActionClicked() {
//				asyncUnmarkDeleteWord(wordcard);
//				mDeletedWord = null;
//				mUndoShowing = false;
//			}
//		}, 0, getResources().getString(R.string.word_reviewed), true,
//				R.string.word_reviewed_undo, true);
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
		private CharSequence mDrawerTitle;
		private CharSequence mTitle;

		private ActionBarHelper() {
			mActionBar = ((SherlockFragmentActivity) getActivity()).getSupportActionBar();
		}

		public void init() {
			mActionBar.setDisplayHomeAsUpEnabled(true);
			mActionBar.setHomeButtonEnabled(true);
			mTitle = mDrawerTitle = getActivity().getTitle();
		}

		/**
		 * When the drawer is closed we restore the action bar state reflecting
		 * the specific contents in view.
		 */
		public void onDrawerClosed() {
			mActionBar.setTitle(mTitle);
		}

		/**
		 * When the drawer is open we set the action bar to a generic title. The
		 * action bar should only contain data relevant at the top level of the
		 * nav hierarchy represented by the drawer, as the rest of your content
		 * will be dimmed down and non-interactive.
		 */
		public void onDrawerOpened() {
			mActionBar.setTitle(mDrawerTitle);
		}

		public void setTitle(CharSequence title) {
			mTitle = title;
		}
	}
	
	/***
     * Handle the delete word cards confirmation dialog
     */

    private void showConfirmationDialog() {
        Resources res = getResources();
        String msg = String.format(res.getQuantityText(R.plurals.word_card_delete_confirmation, mAdapter.getSelectedItemsNum()).toString());
        
        DialogFragment frag = WordsDeleteConfirmationDialog.newInstance(msg);
        frag.setTargetFragment(this, 0);
        frag.show(getFragmentManager(), msg);
        mInDeleteConfirmation = true;
    }

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == -1) {
            if (mAdapter != null) {
                mAdapter.deleteSelectedWordCards();
                mActionMode.finish();
            }
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
			showConfirmationDialog();
			break;
		default:
			break;
		}
		return false;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		if(mAdapter != null) {
            mAdapter.clearSelectedAlarms();
        }
        mActionMode = null;
	}

	@Override
	public boolean onLongClick(View v) {
		mAdapter.toggleSelectState(v);
        mAdapter.notifyDataSetChanged();
        updateActionMode();
        return false;
	}
}
