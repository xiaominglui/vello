package com.mili.xiaominglui.app.vello.adapter;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.card.ReviewCard;
import com.mili.xiaominglui.app.vello.data.model.MiliDictionaryItem;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent;

import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardCursorAdapter;
import it.gmariotti.cardslib.library.internal.CardThumbnail;
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

        //Add the thumbnail
        if (!TextUtils.isEmpty(card.urlResourceThumb)) {
            CardThumbnail thumb = new CardThumbnail(mContext);
            thumb.setUrlResource(card.urlResourceThumb);
            thumb.setErrorResource(card.errorResourceIdThumb);
            card.addCardThumbnail(thumb);
        }

        return card;
    }

    private void setCardFromCursor(ReviewCard card, Cursor cursor) {
        String jsonString = cursor.getString(VelloContent.DbWordCard.Columns.DESC.getIndex());
        Gson gson = new Gson();
        MiliDictionaryItem item = gson.fromJson(jsonString, MiliDictionaryItem.class);
        if (item != null) {
            card.mainTitle = item.spell;
            card.secondaryTitle = "";
            card.urlResourceThumb = item.pic;
            card.errorResourceIdThumb = R.drawable.ic_launcher;
        }
    }
}
