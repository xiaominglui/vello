package com.mili.xiaominglui.app.vello.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.card.ReviewCard;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent;

import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardCursorAdapter;
import it.gmariotti.cardslib.library.view.CardView;

/**
 * Created by xiaominglui on 10/13/14.
 */
public class ReviewCardCursorAdapter extends CardCursorAdapter {


    public ReviewCardCursorAdapter(Context context) {
        super(context);
    }

    @Override
    protected Card getCardFromCursor(Cursor cursor) {
        ReviewCard card = new ReviewCard(super.getContext());
        setCardFromCursor(card, cursor);
        return card;
    }

    private void setCardFromCursor(ReviewCard card, Cursor cursor) {
        card.mainTitle = cursor.getString(VelloContent.DbWordCard.Columns.NAME.getIndex());
        card.secondaryTitle = "hello world!";
    }
}
