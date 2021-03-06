package com.mili.xiaominglui.app.vello.card;


import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardExpand;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.ViewToClickToExpand;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.malinskiy.materialicons.widget.IconTextView;
import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.VaaApplication;
import com.mili.xiaominglui.app.vello.base.C;
import com.mili.xiaominglui.app.vello.base.log.L;
import com.mili.xiaominglui.app.vello.config.VelloConfig;
import com.mili.xiaominglui.app.vello.data.model.Acceptation;
import com.mili.xiaominglui.app.vello.data.model.MiliDictionaryItem;
import com.mili.xiaominglui.app.vello.data.model.Pronunciation;
import com.mili.xiaominglui.app.vello.data.model.TrelloCard;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbWordCard;
import com.mili.xiaominglui.app.vello.util.AccountUtils;
import com.rey.material.widget.Button;

public class ReviewCard extends Card {
	private static final String TAG = ReviewCard.class.getSimpleName();
    private static final int STATUS_REVIEW_INIT = 0;
    private static final int STATUS_REVIEW_TO_RECALL = 1;
    private static final int STATUS_REVIEW_TO_RELEARN = 2;

    private Button mLeft;
    private Button mRight;
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
    public MiliDictionaryItem dictItem;

    public int errorResourceIdThumb;
    public String urlResourceThumb;
    public int reviewProgress;

    private Card mCard;

    private int mStatus;

    private OnCardReviewedListener mListener;

    public ReviewCard(Context context) {
        super(context, R.layout.review_card_inner_content);
        mCard = this;
        mStatus = STATUS_REVIEW_INIT;
    }

    public interface OnCardReviewedListener {
        void onReviewed(Card card);
    }

    public void setOnCardReviewedListener (OnCardReviewedListener listener) {
        mListener = listener;
    }
	
	public void init() {
        L.d(TAG, "init()---" + mStatus);
        setShadow(false);
        ReviewCardThumbnail thumb;
        if (!TextUtils.isEmpty(urlResourceThumb)) {
            thumb = new ReviewCardThumbnail(C.get(), urlResourceThumb);
            thumb.setExternalUsage(true);
            addCardThumbnail(thumb);
        }

		CardHeader header = new ReviewCardHeader(mContext);
		header.setTitle(mainTitle);
		addCardHeader(header);

        ViewToClickToExpand viewToClickToExpand = ViewToClickToExpand.builder().enableForExpandAction();
        setViewToClickToExpand(viewToClickToExpand);

        ReviewCardExpand expand = new ReviewCardExpand(mContext, dictItem);
        addCardExpand(expand);
	}

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        L.d(TAG, "setupInnerViewElements---"+mStatus+"/"+mainTitle);
        mLeft = (Button) parent.findViewById(R.id.left);
        mRight = (Button) parent.findViewById(R.id.right);
        if (mStatus == STATUS_REVIEW_INIT) {
            L.d(TAG, "init");
            mLeft.setText(C.get().getString(R.string.title_button_relearn));
            mRight.setText(C.get().getString(R.string.title_button_recalled));
            mLeft.setVisibility(View.VISIBLE);
            mRight.setVisibility(View.VISIBLE);
        }

        mLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v instanceof Button) {
                    Button button = (Button) v;
                    String status = button.getText().toString();
                    if (status.equals(C.get().getString(R.string.title_button_relearn))) {
                        mStatus = STATUS_REVIEW_TO_RELEARN;
                        mCard.doExpand();
                        mLeft.setVisibility(View.INVISIBLE);
                        mRight.setText(C.get().getString(R.string.title_button_relearned));
                    } else if (status.equals(C.get().getString(R.string.title_button_incorrect))) {
                        if (mListener != null) {
                            mListener.onReviewed(mCard);
                        }
                        asyncMarkRelearnedWord();
                    }
                }

            }
        });

        mRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v instanceof Button) {
                    Button button = (Button) v;
                    String status = button.getText().toString();
                    if (status.equals(C.get().getString(R.string.title_button_recalled))) {
                        mStatus = STATUS_REVIEW_TO_RECALL;
                        mCard.doExpand();
                        mLeft.setText(C.get().getString(R.string.title_button_incorrect));
                        mRight.setText(C.get().getString(R.string.title_button_correct));
                    } else if (status.equals(C.get().getString(R.string.title_button_correct))) {
                        if (mListener != null) {
                            mListener.onReviewed(mCard);
                        }
                        asyncMarkRecalledWord();
                    } else if (status.equals(C.get().getString(R.string.title_button_relearned))) {
                        if (mListener != null) {
                            mListener.onReviewed(mCard);
                        }
                        asyncMarkRelearnedWord();
                    }
                }

            }
        });
//        mReviewButton.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                if (deleted) {
//                    if (relearned) {
//                        setReviewButtionStatus(BUTTON_STATUS_RELEARNED);
//                    } else {
//                        setReviewButtionStatus(BUTTON_STATUS_TO_RECALL);
//                    }
//                } else {
//                    setReviewButtionStatus(BUTTON_STATUS_TO_DELETE);
//                }
//                markDeleted(!deleted);
//                return true;
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

    private void asyncMarkDeleteWordRemotely() {
        final AsyncTask<Void, Void, Void> deleteTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                L.d(TAG, "mark Card#"
                        + idInLocalDB
                        + "deleted. --- "
                        + mainTitle);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
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
                L.d(TAG, "unmark Card#"
                        + idInLocalDB
                        + "recalled. --- "
                        + mainTitle);
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

    public void asyncMarkRelearnedWord() {
        final AsyncTask<Void, Void, Void> markTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                L.d(TAG, "mark Card#"
                        + idInLocalDB
                        + "relearned. --- "
                        + mainTitle);
                ContentValues cv = new ContentValues();

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                Calendar rightNow = Calendar.getInstance();
                long rightNowUnixTime = rightNow.getTimeInMillis();

                long rightNowUnixTimeGMT = rightNowUnixTime - TimeZone.getDefault().getRawOffset();
                long delta = VelloConfig.RELEARNED_DUE_DELTA;
                long dueUnixTime = rightNowUnixTimeGMT + delta;

                Date dueDate = new Date(dueUnixTime);
                String stringDueDate = format.format(dueDate);
                cv.put(DbWordCard.Columns.DUE.getName(), stringDueDate);

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
	
	public void asyncMarkRecalledWord() {
		final AsyncTask<Void, Void, Void> markTask = new AsyncTask<Void, Void, Void>() {

            @Override
			protected Void doInBackground(Void... voids) {
                L.d(TAG, "mark Card#"
                        + idInLocalDB
                        + "recalled. --- "
                        + mainTitle);
                ContentValues cv = new ContentValues();

                int positionList = AccountUtils.getVocabularyListPosition(mContext, idList);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
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

    class ReviewCardExpand extends CardExpand {
        private MiliDictionaryItem mData;

        public ReviewCardExpand(Context context, MiliDictionaryItem data) {
            super(context,R.layout.card_review_expand_layout);
            mData = data;
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {

            View root = view.findViewById(R.id.card_review_expand);

            LinearLayout linearLayoutPhoneticArea = (LinearLayout) root.findViewById(R.id.phonetics_area);
            LinearLayout linearLayoutDefinitionArea = (LinearLayout) root.findViewById(R.id.definition_area);

            //It is just an example. You should load your images in an async way
            if (mData != null){
                linearLayoutPhoneticArea.removeAllViews();
                linearLayoutDefinitionArea.removeAllViews();

                if (mData.pron != null && mData.pron.length > 0) {
                    for (final Pronunciation pronunciation : mData.pron) {
                        View phoneticsView = LayoutInflater.from(mContext).inflate(R.layout.phonetics_item, null);
                        LinearLayout phoneticsGroup = (LinearLayout) phoneticsView.findViewById(R.id.phonetics_group);
                        String phoneticsType = "";
                        if (pronunciation.type.equals("en")) {
                            phoneticsType = mContext.getResources().getString(R.string.title_phonetics_uk);
                        } else if (pronunciation.type.equals("us")) {
                            phoneticsType = mContext.getResources().getString(R.string.title_phonetics_us);
                        }

                        ((TextView) phoneticsView.findViewById(R.id.phonetics_type)).setText(phoneticsType);
                        ((TextView) phoneticsView.findViewById(R.id.phonetics_symbol)).setText("[" + pronunciation.ps + "]");
                        IconTextView soundIcon = (IconTextView) phoneticsView.findViewById(R.id.phonetics_sound);
                        if (!TextUtils.isEmpty(pronunciation.link)) {
                            soundIcon.setVisibility(View.VISIBLE);

                            phoneticsView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    VaaApplication.getAsyncPlayer().play(mContext, Uri.parse(pronunciation.link), false, AudioManager.STREAM_MUSIC);
                                }
                            });
                            } else {
                            soundIcon.setVisibility(View.GONE);
                        }

                        linearLayoutPhoneticArea.addView(phoneticsGroup);
                    }
                }

                if (mData.accettation != null && mData.accettation.length > 0) {
                    for (Acceptation acce : mData.accettation) {
                        View definitionView = LayoutInflater.from(mContext).inflate(R.layout.definition_item, null);
                        LinearLayout definiitionGroup = (LinearLayout) definitionView.findViewById(R.id.definition_group);
                        ((TextView) definitionView.findViewById(R.id.pos)).setText(acce.pos);

                        ((TextView) definitionView.findViewById(R.id.definiens)).setText(acce.accep);
                        linearLayoutDefinitionArea.addView(definiitionGroup);
                    }
                }
            }
        }
    }
}
