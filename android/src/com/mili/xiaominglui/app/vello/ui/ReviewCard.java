package com.mili.xiaominglui.app.vello.ui;

import com.mili.xiaominglui.app.vello.R;

import android.content.Context;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;

public class ReviewCard extends Card {
	protected String mTitleHeader;

	public ReviewCard(Context context) {
		super(context, R.layout.card_review_inner_content);
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
        return 0;
    }

}
