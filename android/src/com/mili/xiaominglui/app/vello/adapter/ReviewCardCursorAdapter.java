package com.mili.xiaominglui.app.vello.adapter;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.card.ReviewCard;
import com.mili.xiaominglui.app.vello.data.model.MiliDictionaryItem;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent;
import com.mili.xiaominglui.app.vello.util.AccountUtils;

import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardCursorAdapter;
import it.gmariotti.cardslib.library.internal.CardCursorMultiChoiceAdapter;
import it.gmariotti.cardslib.library.internal.CardThumbnail;
import it.gmariotti.cardslib.library.view.CardView;

/**
 * Created by xiaominglui on 10/13/14.
 */
public class ReviewCardCursorAdapter extends CardCursorMultiChoiceAdapter {


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
        } else {
            switch (card.reviewProgress) {
                case 0:
                case 1:
                    card.setBackgroundResourceId(R.drawable.demo_card_selector_color1);
                    break;
                case 2:
                    card.setBackgroundResourceId(R.drawable.demo_card_selector_color3);
                    break;
                case 3:
                    card.setBackgroundResourceId(R.drawable.demo_card_selector_color4);
                    break;
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                    card.setBackgroundResourceId(R.drawable.demo_card_selector_color5);
                    break;

            }
        }

        return card;
    }

    private void setCardFromCursor(ReviewCard card, Cursor cursor) {
        String jsonString = cursor.getString(VelloContent.DbWordCard.Columns.DESC.getIndex());

        Gson gson = new Gson();
        MiliDictionaryItem item = gson.fromJson(jsonString, MiliDictionaryItem.class);
        if (item != null) {
            String idList = cursor.getString(VelloContent.DbWordCard.Columns.LIST_ID.getIndex());
            card.position = cursor.getPosition();
            card.mainTitle = item.spell;
            card.secondaryTitle = "";
            card.idList = idList;
            card.reviewProgress = AccountUtils.getVocabularyListPosition(mContext, idList);
            card.idInLocalDB = cursor.getInt(VelloContent.DbWordCard.Columns.ID.getIndex());
            card.urlResourceThumb = item.pic;
            card.errorResourceIdThumb = R.drawable.ic_launcher;
            card.init();
        }
    }

    @Override
    public int getPosition(Card card) {
        return ((ReviewCard )card).position;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        return false;
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked, CardView cardView, Card card) {
        Toast.makeText(getContext(), "Click;" + position + " - " + checked, Toast.LENGTH_SHORT).show();
    }
}
