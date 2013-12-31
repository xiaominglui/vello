package com.mili.xiaominglui.app.vello.ui;

import it.gmariotti.cardslib.library.internal.Card;
import android.content.Context;

public class FloatDictCard extends Card {
	private static final String TAG = FloatDictCard.class.getSimpleName();
	
	public FloatDictCard(Context context) {
		super(context);
		init();
	}
	
	public void init() {
		FloatDictCardHeader header = new FloatDictCardHeader(mContext);
		header.setButtonExpandVisible(false);
		addCardHeader(header);

		setClickable(false);
		setLongClickable(false);
		setSwipeable(false);
	}
}
