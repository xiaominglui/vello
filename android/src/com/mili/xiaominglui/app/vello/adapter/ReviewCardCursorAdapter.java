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
        CardThumbnail thumb = new CardThumbnail(mContext);
        if (card.urlResourceThumb != null && !TextUtils.isEmpty(card.urlResourceThumb)) {
            thumb.setUrlResource(card.urlResourceThumb);
            thumb.setErrorResource(card.errorResourceIdThumb);
        } else {
            thumb.setDrawableResource(card.errorResourceIdThumb);
        }
        card.addCardThumbnail(thumb);

        return card;
    }

    private void setCardFromCursor(ReviewCard card, Cursor cursor) {
        card.mainTitle = cursor.getString(VelloContent.DbWordCard.Columns.NAME.getIndex());
        card.secondaryTitle = "";
        String jsonString = cursor.getString(VelloContent.DbWordCard.Columns.DESC.getIndex());

        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(jsonString).getAsJsonObject();
        card.urlResourceThumb = jsonObject.get("pic").getAsString();
        card.errorResourceIdThumb = R.drawable.ic_launcher;
    }
}
