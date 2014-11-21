package com.mili.xiaominglui.app.vello.card;


import at.markushi.ui.CircleButton;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;

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
	
	public TrelloCard trelloCard;
	
	public String markDeleted;
	public String dateLastOperation;
	
	public int idInLocalDB;

    public String mainTitle;
    public String secondaryTitle;
    public int errorResourceIdThumb;
    public String urlResourceThumb;
    public int reviewProgress;

    public ReviewCard(Context context) {
        super(context, R.layout.review_card_inner_content);

    }
	
	public ReviewCard(Context context, Cursor c) {
		super(context, R.layout.card_review_inner_content);
		trelloCard = new TrelloCard(c);
		
		markDeleted = c.getString(DbWordCard.Columns.MARKDELETED.getIndex());
		dateLastOperation = c.getString(DbWordCard.Columns.DATE_LAST_OPERATION.getIndex());
		
		idInLocalDB = c.getInt(DbWordCard.Columns.ID.getIndex());
	}
	
	public void init() {
		CardHeader header = new ReviewCardHeader(mContext);
		header.setTitle(mainTitle);
		header.setButtonExpandVisible(true);

        header.setOtherButtonVisible(true);

        //Add a callback
        header.setOtherButtonClickListener(new CardHeader.OnClickCardHeaderOtherButtonListener() {
            @Override
            public void onButtonItemClick(Card card, View view) {
                Toast.makeText(mContext, "Click on Other Button", Toast.LENGTH_LONG).show();
            }
        });
		addCardHeader(header);


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
	
	private void asyncUnmarkRecalledWord(final Card reviewCard) {
		final AsyncTask<Card, Void, Void> unmarkTask = new AsyncTask<Card, Void, Void>() {

			@Override
			protected Void doInBackground(Card... reviewCards) {
				for (final Card reviewCard : reviewCards) {
					if (VelloConfig.DEBUG_SWITCH) {
						Log.d(TAG, "unmark Card#"
								+ ((ReviewCard) reviewCard).idInLocalDB
								+ "recalled. --- "
								+ ((ReviewCard) reviewCard).trelloCard.name);
					}
					ContentValues cv = new ContentValues();
					cv.put(DbWordCard.Columns.CLOSED.getName(), ((ReviewCard)reviewCard).trelloCard.closed);
					cv.put(DbWordCard.Columns.DUE.getName(), ((ReviewCard)reviewCard).trelloCard.due);
					cv.put(DbWordCard.Columns.LIST_ID.getName(), ((ReviewCard)reviewCard).trelloCard.idList);
					cv.put(DbWordCard.Columns.DATE_LAST_OPERATION.getName(), "");
					Uri uri = ContentUris.withAppendedId(DbWordCard.CONTENT_URI, ((ReviewCard)reviewCard).idInLocalDB);
					mContext.getContentResolver().update(uri, cv, null, null);
				}
				return null;
			}
		};
		unmarkTask.execute(reviewCard);
	}
	
	@SuppressLint("SimpleDateFormat")
	private void asyncMarkRecalledWord(final Card reviewCards) {
		final AsyncTask<Card, Void, Void> markTask = new AsyncTask<Card, Void, Void>() {

			@Override
			protected Void doInBackground(Card... reviewCards) {
				for (final Card reviewCard : reviewCards) {
					if (VelloConfig.DEBUG_SWITCH) {
						Log.d(TAG, "mark Card#"
								+ ((ReviewCard) reviewCard).idInLocalDB
								+ "recalled. --- "
								+ ((ReviewCard) reviewCard).trelloCard.name);
					}
					ContentValues cv = new ContentValues();

					int positionList = AccountUtils.getVocabularyListPosition(mContext, ((ReviewCard) reviewCard).trelloCard.idList);
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
					Uri uri = ContentUris.withAppendedId(DbWordCard.CONTENT_URI, ((ReviewCard) reviewCard).idInLocalDB);
					mContext.getContentResolver().update(uri, cv, null, null);
				}
				return null;
			}
		};
		markTask.execute(reviewCards);
	}
}
