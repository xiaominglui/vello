package com.mili.xiaominglui.app.vello.adapter;

import java.util.List;

import android.content.Context;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;

public class BaseCardArrayAdapter extends CardArrayAdapter {
	
	public BaseCardArrayAdapter(Context context, List<Card> cards) {
		super(context, cards);
	}

}
