package com.mili.xiaominglui.app.vello.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;


import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;

public class ReviewCardArrayAdapter extends CardArrayAdapter {
    public ReviewCardArrayAdapter(Context context, List<Card> cards) {
        super(context, cards);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }
}
