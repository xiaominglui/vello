package com.mili.xiaominglui.app.vello.ui;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardExpand;
import it.gmariotti.cardslib.library.internal.CardHeader;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbWordCard;

public class ReviewCard extends Card {
	protected TextView mTitle;
    protected TextView mSecondaryTitle;
	
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
		
		init(context);
	}
	
	private void init(Context context) {
		CardHeader header = new CardHeader(context);
		header.setTitle(name);
		header.setButtonExpandVisible(true);
		addCardHeader(header);
		
		
		CardExpand expand = new CardExpand(context);
		expand.setTitle(desc);
		addCardExpand(expand);
	}
	
	@Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
		//Retrieve elements
        mTitle = (TextView) parent.findViewById(R.id.carddemo_myapps_main_inner_title);
        mSecondaryTitle = (TextView) parent.findViewById(R.id.carddemo_myapps_main_inner_secondaryTitle);
        
        if (mTitle!=null)
            mTitle.setText("Google");

        if (mSecondaryTitle!=null)
            mSecondaryTitle.setText("Map");
		
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
