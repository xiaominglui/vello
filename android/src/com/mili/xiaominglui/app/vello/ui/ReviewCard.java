package com.mili.xiaominglui.app.vello.ui;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardExpand;
import it.gmariotti.cardslib.library.internal.CardHeader;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.atermenji.android.iconictextview.IconicTextView;
import com.atermenji.android.iconictextview.icon.FontAwesomeIcon;
import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbWordCard;
import com.mili.xiaominglui.app.vello.util.AccountUtils;

public class ReviewCard extends Card {
    protected IconicTextView iconicLifeSign;
	protected TextView textViewLifeCount;
	
	public int idInLocalDB;
	
	public String id;
	public String name;
	public String desc;
	public String due;
	public String idList;
	public String closed;
	public String dateLastActivity;
	public String markDeleted;
	public String dateLastOperation;
	
	public ReviewCard(Context context, Cursor c) {
		super(context, R.layout.card_review_inner_content);
		
		idInLocalDB = c.getInt(DbWordCard.Columns.ID.getIndex());

		id = c.getString(DbWordCard.Columns.CARD_ID.getIndex());
		name = c.getString(DbWordCard.Columns.NAME.getIndex());
		desc = c.getString(DbWordCard.Columns.DESC.getIndex());
		due = c.getString(DbWordCard.Columns.DUE.getIndex());
		idList = c.getString(DbWordCard.Columns.LIST_ID.getIndex());
		closed = c.getString(DbWordCard.Columns.CLOSED.getIndex());
		dateLastActivity = c.getString(DbWordCard.Columns.DATE_LAST_ACTIVITY.getIndex());
		markDeleted = c.getString(DbWordCard.Columns.MARKDELETED.getIndex());
		dateLastOperation = c.getString(DbWordCard.Columns.DATE_LAST_OPERATION.getIndex());
		
		init();
	}
	
	private void init() {
		CardHeader header = new CardHeader(mContext);
		header.setTitle(name);
		header.setButtonExpandVisible(true);
		addCardHeader(header);
		
		
		ReviewExpandCard expand = new ReviewExpandCard(mContext, desc);
		expand.setTitle(desc);
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
        	int positionList = AccountUtils.getVocabularyListPosition(mContext, idList);
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
		cv.put(DbWordCard.Columns.CARD_ID.getName(), id);
		cv.put(DbWordCard.Columns.NAME.getName(), name);
		cv.put(DbWordCard.Columns.DESC.getName(), desc);
		cv.put(DbWordCard.Columns.DUE.getName(), due);
		cv.put(DbWordCard.Columns.CLOSED.getName(), closed);
		cv.put(DbWordCard.Columns.LIST_ID.getName(), idList);
		cv.put(DbWordCard.Columns.DATE_LAST_ACTIVITY.getName(), dateLastActivity);
		cv.put(DbWordCard.Columns.MARKDELETED.getName(), markDeleted);
		cv.put(DbWordCard.Columns.DATE_LAST_OPERATION.getName(), dateLastOperation);

		return cv;
	}

}
