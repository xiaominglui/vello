package com.mili.xiaominglui.app.vello.ui;

import com.mili.xiaominglui.app.vello.R;

import android.content.Context;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;

public class DictCard extends Card {
	protected String mTitleHeader;
	protected String mDictData;

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
