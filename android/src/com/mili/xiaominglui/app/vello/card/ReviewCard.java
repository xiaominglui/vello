package com.mili.xiaominglui.app.vello.card;


import at.markushi.ui.CircleButton;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.base.BaseCard;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.IconTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.atermenji.android.iconictextview.IconicTextView;
import com.atermenji.android.iconictextview.icon.FontAwesomeIcon;
import com.joanzapata.android.iconify.Iconify;
import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.config.VelloConfig;
import com.mili.xiaominglui.app.vello.data.model.TrelloCard;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbWordCard;
import com.mili.xiaominglui.app.vello.ui.ReviewCardHeader;
import com.mili.xiaominglui.app.vello.ui.ReviewExpandCard;
import com.mili.xiaominglui.app.vello.util.AccountUtils;

public class ReviewCard extends Card {
	private static final String TAG = ReviewCard.class.getSimpleName();
    protected IconicTextView iconicLifeSign;
	protected TextView textViewLifeCount;
    private CircleButton mReviewButton;
	
	public TrelloCard trelloCard;
	
	public String markDeleted;
	public String dateLastOperation;
	


    public String mainTitle;
    public String secondaryTitle;
    public String idList;
    public int idInLocalDB;
    public int position;
    public String trelloID;
    public String closed;
    public String due;

    public int errorResourceIdThumb;
    public String urlResourceThumb;
    public int reviewProgress;

    private OnClickReviewCardButtonsListener mReviewCardButtonsOnClickListener;
    private Card mCard;

    private boolean deleted;
    private boolean relearned;

    public ReviewCard(Context context) {
        super(context, R.layout.review_card_inner_content);
        mCard = this;
        deleted = false;
        relearned = false;

    }
	
	public ReviewCard(Context context, Cursor c) {
		super(context, R.layout.card_review_inner_content);
		trelloCard = new TrelloCard(c);
		
		markDeleted = c.getString(DbWordCard.Columns.MARKDELETED.getIndex());
		dateLastOperation = c.getString(DbWordCard.Columns.DATE_LAST_OPERATION.getIndex());
		
	}

    /**
     * Interface to handle callbacks when Reviewed Button is clicked
     */
    public interface OnClickReviewCardButtonsListener {
        public void onReviewedButtonClicked(Card card, View view);
        public void onRecallButtonClicked(Card card, View view);
    }

    public void markDeleted() {
        deleted = true;
    }

    public void markRelearned() {
        relearned = true;
    }
	
	public void init() {
		CardHeader header = new ReviewCardHeader(mContext);
		header.setTitle(mainTitle);

		addCardHeader(header);

        setSwipeable(true);
        setOnSwipeListener(new OnSwipeListener() {
            @Override
            public void onSwipe(Card card) {
                if (deleted) {
                    Toast.makeText(getContext(), "Delete card: " + mainTitle, Toast.LENGTH_SHORT).show();
                    asyncMarkRecalledWord();
                    asyncMarkDeleteWordRemotely();
                } else if (relearned) {
                    // relearned TODO
                    Toast.makeText(getContext(), "Relearned card: " + mainTitle, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Recall card: " + mainTitle, Toast.LENGTH_SHORT).show();
                    asyncMarkRecalledWord();
                }
            }
        });

        setOnUndoSwipeListListener(new OnUndoSwipeListListener() {
            @Override
            public void onUndoSwipe(Card card) {
                Toast.makeText(getContext(), "Undo card: " + mainTitle, Toast.LENGTH_SHORT).show();
                asyncUnmarkRecalledWord();
            }
        });

        setOnUndoHideSwipeListListener(new OnUndoHideSwipeListListener() {
            @Override
            public void onUndoHideSwipe(Card card) {
            }
        });

//		ReviewExpandCard expand = new ReviewExpandCard(mContext, trelloCard.desc);
//		expand.setTitle(trelloCard.desc);
//		addCardExpand(expand);
//
//		setClickable(false);
//		setLongClickable(false);
//		setSwipeable(true);
//
//		setOnSwipeListener(new OnSwipeListener() {
//			@Override
//			public void onSwipe(Card card) {
//				asyncMarkRecalledWord(card);
//			}
//		});
//
//		setOnUndoSwipeListListener(new OnUndoSwipeListListener() {
//			@Override
//			public void onUndoSwipe(Card card) {
//				asyncUnmarkRecalledWord(card);
//			}
//		});
//
//		setOnExpandAnimatorEndListener(new Card.OnExpandAnimatorEndListener() {
//            @Override
//            public void onExpandEnd(Card card) {
//            	setSwipeable(false);
//            }
//        });
	}

    public void setReviewButtionStatus(boolean relearned) {
        if (relearned) {
            mReviewButton.setImageResource(R.drawable.ic_action_close);
            if (mReviewCardButtonsOnClickListener != null) {
                mReviewButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mReviewCardButtonsOnClickListener.onReviewedButtonClicked(mCard, view);
                    }
                });
            }

        } else {
            mReviewButton.setImageResource(R.drawable.ic_action_done);
            if (mReviewCardButtonsOnClickListener != null) {
                mReviewButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mReviewCardButtonsOnClickListener.onRecallButtonClicked(mCard, view);
                    }
                });
            }

        }
    }

    public void setReviewCardButtonsOnClickListener(OnClickReviewCardButtonsListener listener) {
        mReviewCardButtonsOnClickListener = listener;
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        mReviewButton = (CircleButton) parent.findViewById(R.id.review);
        setReviewButtionStatus(relearned);
    }

    @Override
    public int getType() {
        return 0;
    }
	
	public ContentValues toContentValues() {
		ContentValues cv = new ContentValues();
		cv.put(DbWordCard.Columns.CARD_ID.getName(), trelloCard.id);
		cv.put(DbWordCard.Columns.NAME.getName(), trelloCard.name);
		cv.put(DbWordCard.Columns.DESC.getName(), trelloCard.desc);
		cv.put(DbWordCard.Columns.DUE.getName(), trelloCard.due);
		cv.put(DbWordCard.Columns.CLOSED.getName(), trelloCard.closed);
		cv.put(DbWordCard.Columns.LIST_ID.getName(), trelloCard.idList);
		cv.put(DbWordCard.Columns.DATE_LAST_ACTIVITY.getName(), trelloCard.dateLastActivity);
		cv.put(DbWordCard.Columns.MARKDELETED.getName(), markDeleted);
		cv.put(DbWordCard.Columns.DATE_LAST_OPERATION.getName(), dateLastOperation);

		return cv;
	}

    private void asyncMarkDeleteWordRemotely() {
        final AsyncTask<Void, Void, Void> deleteTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                Calendar rightNow = Calendar.getInstance();
                long rightNowUnixTime = rightNow.getTimeInMillis();
                Date rightNowDate = new Date(rightNowUnixTime);
                String stringRightNow = format.format(rightNowDate);
                ContentValues cv = new ContentValues();
                cv.put(DbWordCard.Columns.MARKDELETED.getName(), "true");
                cv.put(DbWordCard.Columns.DATE_LAST_OPERATION.getName(),
                        stringRightNow);
                Uri uri = ContentUris.withAppendedId(DbWordCard.CONTENT_URI, idInLocalDB);
                getContext().getContentResolver().update(uri, cv, null, null);
                return null;
            }
        };
        deleteTask.execute();
    }
	
	private void asyncUnmarkRecalledWord() {
		final AsyncTask<Void, Void, Void> unmarkTask = new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... voids) {
                if (VelloConfig.DEBUG_SWITCH) {
                    Log.d(TAG, "unmark Card#"
                            + idInLocalDB
                            + "recalled. --- "
                            + mainTitle);
                }
                ContentValues cv = new ContentValues();
                cv.put(DbWordCard.Columns.CLOSED.getName(), closed);
                cv.put(DbWordCard.Columns.DUE.getName(), due);
                cv.put(DbWordCard.Columns.LIST_ID.getName(), idList);
                cv.put(DbWordCard.Columns.DATE_LAST_OPERATION.getName(), "");
                Uri uri = ContentUris.withAppendedId(DbWordCard.CONTENT_URI, idInLocalDB);
                mContext.getContentResolver().update(uri, cv, null, null);
				return null;
			}
		};
		unmarkTask.execute();
	}
	
	@SuppressLint("SimpleDateFormat")
	public void asyncMarkRecalledWord() {
		final AsyncTask<Void, Void, Void> markTask = new AsyncTask<Void, Void, Void>() {

            @Override
			protected Void doInBackground(Void... voids) {
                if (VelloConfig.DEBUG_SWITCH) {
                    Log.d(TAG, "mark Card#"
                            + idInLocalDB
                            + "recalled. --- "
                            + mainTitle);
                }
                ContentValues cv = new ContentValues();

                int positionList = AccountUtils.getVocabularyListPosition(mContext, idList);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                Calendar rightNow = Calendar.getInstance();
                long rightNowUnixTime = rightNow.getTimeInMillis();

                if (positionList == VelloConfig.VOCABULARY_LIST_POSITION_8TH) {
                    cv.put(DbWordCard.Columns.CLOSED.getName(), "true");
                } else {
                    long rightNowUnixTimeGMT = rightNowUnixTime
                            - TimeZone.getDefault().getRawOffset();
                    long delta = VelloConfig.VOCABULARY_LIST_DUE_DELTA[positionList];
                    long dueUnixTime = rightNowUnixTimeGMT + delta;

                    Date dueDate = new Date(dueUnixTime);
                    String stringDueDate = format.format(dueDate);
                    cv.put(DbWordCard.Columns.DUE.getName(), stringDueDate);

                    String newIdList = AccountUtils.getVocabularyListId(mContext, positionList + 1);
                    cv.put(DbWordCard.Columns.LIST_ID.getName(), newIdList);
                }

                Date rightNowDate = new Date(rightNowUnixTime);
                String stringRightNow = format.format(rightNowDate);
                cv.put(DbWordCard.Columns.DATE_LAST_OPERATION.getName(), stringRightNow);
                Uri uri = ContentUris.withAppendedId(DbWordCard.CONTENT_URI, idInLocalDB);
                mContext.getContentResolver().update(uri, cv, null, null);
                return null;
            }
		};
		markTask.execute();
	}
}
