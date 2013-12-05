package com.mili.xiaominglui.app.vello.ui;

import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbDictCard;

import android.content.Context;
import android.database.Cursor;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardExpand;
import it.gmariotti.cardslib.library.internal.CardHeader;

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

}
