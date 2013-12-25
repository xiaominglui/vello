package com.mili.xiaominglui.app.vello.view;

import android.annotation.SuppressLint;
import android.content.Context;
import it.gmariotti.cardslib.library.view.CardView;

public class FloatCardView extends CardView {
	/**
     * FloatCard Model
     */
    public FloatCard mCard;

	public FloatCardView(Context context) {
		super(context);
	}
	
	@Override
	@SuppressLint("NewApi")
	protected void setupListeners() {
		if (mCard instanceof FloatCard) {
			if (((FloatCard) mCard).isTouchable()) {
				this.setOnTouchListener(((FloatCard) mCard).getOnTouchListener());
			}
		} else {
			super.setupListeners();
		}
	}
}
