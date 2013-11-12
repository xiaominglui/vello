package com.mili.xiaominglui.app.vello.adapter;

import java.util.List;

import android.content.Context;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;

public class HomeCardArrayAdapter extends CardArrayAdapter {
	
	public HomeCardArrayAdapter(Context context, List<Card> cards) {
		super(context, cards);
	}
	
	@Override
    public int getViewTypeCount() {
        return 3;
    }

}
