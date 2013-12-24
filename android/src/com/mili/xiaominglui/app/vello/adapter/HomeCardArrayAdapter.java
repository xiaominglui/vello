package com.mili.xiaominglui.app.vello.adapter;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;

import java.util.List;

import android.content.Context;

public class HomeCardArrayAdapter extends CardArrayAdapter {
	
	public HomeCardArrayAdapter(Context context, List<Card> cards) {
		super(context, cards);
	}
	
	@Override
    public int getViewTypeCount() {
        return 3;
    }

}
