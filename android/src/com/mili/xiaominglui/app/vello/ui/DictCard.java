package com.mili.xiaominglui.app.vello.ui;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardExpand;
import it.gmariotti.cardslib.library.internal.CardHeader;

import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbDictCard;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbWordCard;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class DictCard extends Card {
	protected String mTitleHeader;
	protected String mDictData;
	
	public String oid;
	public String keyword;
	public String dictdata;
	
	public DictCard(Context context, Cursor c) {
		super(context, R.layout.card_dict_inner_content);
		
		oid = c.getString(DbDictCard.Columns.OID.getIndex());
		keyword = c.getString(DbDictCard.Columns.KEYWORD.getIndex());
		dictdata = c.getString(DbDictCard.Columns.DICDATA.getIndex());
		
		init(context);
		
	}
	
	private void init(Context context) {
		CardHeader header = new CardHeader(context);
		header.setTitle(keyword);
		header.setButtonExpandVisible(true);
		addCardHeader(header);
		
		CardExpand expand = new CardExpand(context);
		expand.setTitle(dictdata);
		addCardExpand(expand);
	}

	public DictCard(Context context, String keyword, String data) {
		super(context, R.layout.card_dict_inner_content);
		mTitleHeader = keyword;
		mDictData = data;
		init();
	}
	
	private void init() {
		CardHeader header = new CardHeader(getContext());
		header.setTitle(mTitleHeader);
		header.setButtonExpandVisible(true);
		addCardHeader(header);
	}
	
	@Override
    public int getType() {
        return 1;
    }
	
	public ContentValues toContentValues() {
		ContentValues cv = new ContentValues();
//		cv.put(DbWordCard.Columns.CARD_ID.getName(), trelloCard.id);
//		cv.put(DbWordCard.Columns.NAME.getName(), trelloCard.name);
//		cv.put(DbWordCard.Columns.DESC.getName(), trelloCard.desc);
//		cv.put(DbWordCard.Columns.DUE.getName(), trelloCard.due);
//		cv.put(DbWordCard.Columns.CLOSED.getName(), trelloCard.closed);
//		cv.put(DbWordCard.Columns.LIST_ID.getName(), trelloCard.idList);
//		cv.put(DbWordCard.Columns.DATE_LAST_ACTIVITY.getName(), trelloCard.dateLastActivity);
//		cv.put(DbWordCard.Columns.MARKDELETED.getName(), markDeleted);
//		cv.put(DbWordCard.Columns.DATE_LAST_OPERATION.getName(), dateLastOperation);

		return cv;
	}

}
