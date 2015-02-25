package com.mili.xiaominglui.app.vello.fragment;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.internal.dismissanimation.SwipeDismissAnimation;
import it.gmariotti.cardslib.library.view.CardListView;
import it.gmariotti.cardslib.library.view.listener.UndoBarController;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.ActionMode.Callback;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.gson.Gson;
import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.adapter.ReviewCardArrayAdapter;
import com.mili.xiaominglui.app.vello.card.ReviewCardThumbnail;
import com.mili.xiaominglui.app.vello.config.VelloConfig;
import com.mili.xiaominglui.app.vello.data.model.MiliDictionaryItem;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbDictCard;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbWordCard;
import com.mili.xiaominglui.app.vello.data.provider.util.ProviderCriteria;
import com.mili.xiaominglui.app.vello.card.DictCard;
import com.mili.xiaominglui.app.vello.card.ReviewCard;
import com.mili.xiaominglui.app.vello.util.AccountUtils;

public class ReviewViewFragment extends BaseListFragment implements LoaderManager.LoaderCallbacks<Cursor>, OnLongClickListener, Callback, DialogInterface.OnClickListener {
    private static final String TAG = ReviewViewFragment.class.getSimpleName();

    CardListView mListView;
    ActionMode mActionMode;

    protected CardArrayAdapter mCardArrayAdapter;
    SwipeDismissAnimation dismissAnimation;

    private onStatusChangedListener mListener;

    private String mCurFilter = "";
    private boolean mIsSearching = false;

    @Override
    public int getTitleResourceId() {
        return R.string.title_review;
    }

    public static Fragment newInstance() {
        Fragment f = new ReviewViewFragment();
        return f;
    }

    public interface onStatusChangedListener {
        public void onModeChanged(int modeColor);

        public void syncOnAllRecalled();

        public void onWordRecalled();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mActionMode!=null){
            mActionMode.finish();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (onStatusChangedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement onStatusChangedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setRetainInstance(true);
//        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root= inflater.inflate(R.layout.fragment_list_cursor, container, false);
        setupListFragment(root);
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.gray_background));
        hideList(false);
        init();
    }

    private void init() {

        mListView = (CardListView) getActivity().findViewById(R.id.list_cursor);

        // Force start background query to load sessions
        getLoaderManager().restartLoader(0, null, this);
    }

    private void initReviewCards(Cursor data) {
        ArrayList<Card> cards = new ArrayList<Card>();

        for (data.moveToFirst(); !data.isAfterLast(); data.moveToNext()) {
            ReviewCard card =  initReviewCard(data);
            card.setId(card.trelloID);
            cards.add(card);
        }

        mCardArrayAdapter = new ReviewCardArrayAdapter(getActivity(), cards);

        mCardArrayAdapter.setUndoBarUIElements(new UndoBarController.DefaultUndoBarUIElements(){
            @Override
            public SwipeDirectionEnabled isEnabledUndoBarSwipeAction() {
                return SwipeDirectionEnabled.TOPBOTTOM;
            }

            @Override
            public AnimationType getAnimationType() {
                return AnimationType.TOPBOTTOM;
            }
        });

        //Enable undo controller!
        mCardArrayAdapter.setEnableUndo(true);

        dismissAnimation = (SwipeDismissAnimation) new SwipeDismissAnimation(getActivity()).setup(mCardArrayAdapter);

        if (mListView != null) {
            mListView.setAdapter(mCardArrayAdapter);
        }
    }

    private ReviewCard initReviewCard(Cursor data) {
        ReviewCard card = new ReviewCard(getActivity().getApplicationContext());
        setCardFromCursor(card, data);

        card.setReviewCardButtonsOnClickListener(new ReviewCard.OnClickReviewCardButtonsListener() {
            @Override
            public void onRelearnedButtonClicked(Card card, View view) {
                dismissAnimation.setDismissRight(true);
                ((ReviewCard) card).markRelearned();
                dismissAnimation.animateDismiss(card);
            }

            @Override
            public void onRecallButtonClicked(Card card, View view) {
                dismissAnimation.setDismissRight(true);
                dismissAnimation.animateDismiss(card);
            }

            @Override
            public void onRemoveButtonClicked(Card card, View view) {
                dismissAnimation.setDismissRight(true);
                dismissAnimation.animateDismiss(card);
            }
        });

        //Add the thumbnail
        ReviewCardThumbnail thumb;
        if (!TextUtils.isEmpty(card.urlResourceThumb)) {
            thumb = new ReviewCardThumbnail(getActivity().getApplicationContext(), card.urlResourceThumb);
        } else {
            thumb = new ReviewCardThumbnail(getActivity().getApplicationContext(), null);
        }
        thumb.setExternalUsage(true);
        card.addCardThumbnail(thumb);
        return card;
    }

    private void setCardFromCursor(ReviewCard card, Cursor cursor) {
        String jsonString = cursor.getString(VelloContent.DbWordCard.Columns.DESC.getIndex());

        Gson gson = new Gson();
        MiliDictionaryItem item = gson.fromJson(jsonString, MiliDictionaryItem.class);
        if (item != null) {
            String idList = cursor.getString(VelloContent.DbWordCard.Columns.LIST_ID.getIndex());
            card.position = cursor.getPosition();
            card.trelloID = cursor.getString(DbWordCard.Columns.CARD_ID.getIndex());
            card.closed = cursor.getString(DbWordCard.Columns.CLOSED.getIndex());
            card.due = cursor.getString(DbWordCard.Columns.DUE.getIndex());
            card.dictItem = item;
            card.mainTitle = item.spell;
            card.secondaryTitle = "";
            card.idList = idList;
            card.reviewProgress = AccountUtils.getVocabularyListPosition(getActivity().getApplicationContext(), idList);
            card.idInLocalDB = cursor.getInt(VelloContent.DbWordCard.Columns.ID.getIndex());
            card.urlResourceThumb = item.pic;
            card.errorResourceIdThumb = R.drawable.ic_launcher;
            card.init();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

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

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            long rightNowUnixTime = rightNow.getTimeInMillis();
            long rightNowUnixTimeGMT = rightNowUnixTime - TimeZone.getDefault().getRawOffset();
            String now = format.format(new Date(rightNowUnixTimeGMT));
            criteria.addEq(DbWordCard.Columns.MARKDELETED, "false");
            criteria.addLt(DbWordCard.Columns.DUE, now, true);
            criteria.addNe(DbWordCard.Columns.CLOSED, "true");
            return new CursorLoader(getActivity(), DbWordCard.CONTENT_URI,
                    DbWordCard.PROJECTION, criteria.getWhereClause(),
                    criteria.getWhereParams(), criteria.getOrderClause());
        } else {
            // Dictionary Mode
            mIsSearching = true;
            mListener.onModeChanged(VelloConfig.DICTIONARY_MODE_ACTION_BAR_COLOR);
            criteria.addLike(DbDictCard.Columns.KEYWORD, mCurFilter + "%");
            return new CursorLoader(getActivity(), DbDictCard.CONTENT_URI,
                    DbDictCard.PROJECTION, criteria.getWhereClause(),
                    criteria.getWhereParams(), criteria.getOrderClause());
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mIsSearching) {
            // in Dictionary Mode
            if (data == null)
                return;
            for (data.moveToFirst(); !data.isAfterLast(); data.moveToNext()) {
                DictCard dc = new DictCard(getActivity()
                        .getApplicationContext(), data);
                dc.setSwipeable(false);

                mCardArrayAdapter.add(dc);
            }
        } else {
            // in Review Mode
            if (getActivity() == null) {
                return;
            }

            initReviewCards(data);
            displayList();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (VelloConfig.DEBUG_SWITCH) {
            Log.d(TAG, "onLoaderReset");
        }
        mCardArrayAdapter.clear();
//        mAdapter.swapCursor(null);
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
            mActionBar = ((SherlockFragmentActivity) getActivity())
                    .getSupportActionBar();
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
