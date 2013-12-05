package com.mili.xiaominglui.app.vello.ui;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.atermenji.android.iconictextview.IconicTextView;
import com.atermenji.android.iconictextview.icon.FontAwesomeIcon;
import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.data.model.TrelloCard;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbWordCard;
import com.mili.xiaominglui.app.vello.util.AccountUtils;

public class ReviewCard extends Card {
    protected IconicTextView iconicLifeSign;
	protected TextView textViewLifeCount;
	
	public TrelloCard trelloCard;
	
	public String markDeleted;
	public String dateLastOperation;
	
	public ReviewCard(Context context, Cursor c) {
		super(context, R.layout.card_review_inner_content);
		trelloCard = new TrelloCard(c);
		
		markDeleted = c.getString(DbWordCard.Columns.MARKDELETED.getIndex());
		dateLastOperation = c.getString(DbWordCard.Columns.DATE_LAST_OPERATION.getIndex());
		
		init();
	}
	
	private void init() {
		CardHeader header = new CardHeader(mContext);
		header.setTitle(trelloCard.name);
		header.setButtonExpandVisible(true);
		addCardHeader(header);
		
		
		ReviewExpandCard expand = new ReviewExpandCard(mContext, trelloCard.desc);
		expand.setTitle(trelloCard.desc);
		addCardExpand(expand);
	}
	
	@Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
		//Retrieve elements
        iconicLifeSign = (IconicTextView) parent.findViewById(R.id.life_sign);
        textViewLifeCount = (TextView) parent.findViewById(R.id.life_count);
        
        if (iconicLifeSign != null) {
        	iconicLifeSign.setIcon(FontAwesomeIcon.CHECK);
        	iconicLifeSign.setTextColor(Color.GRAY);
        }
            

        if (textViewLifeCount != null) {
        	int positionList = AccountUtils.getVocabularyListPosition(mContext, trelloCard.idList);
        	textViewLifeCount.setText(String.valueOf(positionList) + "/9");
			textViewLifeCount.setTextColor(Color.GRAY);
        }
		
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
}
