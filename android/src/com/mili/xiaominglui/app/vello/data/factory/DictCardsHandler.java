package com.mili.xiaominglui.app.vello.data.factory;

import java.io.IOException;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.mili.xiaominglui.app.vello.data.model.DictCard;
import com.mili.xiaominglui.app.vello.data.model.DictCards;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbDictCard;
import com.mili.xiaominglui.app.vello.util.Lists;

import android.content.ContentProviderOperation;
import android.content.Context;

public class DictCardsHandler extends JSONHandler {
	private static final String TAG = DictCardsHandler.class.getSimpleName();

	public DictCardsHandler(Context context) {
		super(context);
	}

	@Override
	public ArrayList<ContentProviderOperation> parse(String json) throws IOException {
        final ArrayList<ContentProviderOperation> batch = Lists.newArrayList();
        DictCards dictCardsJson = new Gson().fromJson(json, DictCards.class);
        int noOfDictCards = dictCardsJson.dictCards.length;
        for (int i = 0; i < noOfDictCards; i++) {
            parseDictCard(dictCardsJson.dictCards[i], batch);
        }
        return batch;
    }

    private static void parseDictCard(DictCard dictCard, ArrayList<ContentProviderOperation> batch) {
        ContentProviderOperation.Builder builder = ContentProviderOperation
                .newInsert(DbDictCard.CONTENT_URI);
        // TODO
        builder.withValue(DbDictCard.Columns.ID.getName(), dictCard._id.$oid);
        builder.withValue(DbDictCard.Columns.SPELL.getName(), dictCard.spell);
        builder.withValue(DbDictCard.Columns.PIC.getName(), dictCard.pic);
        batch.add(builder.build());
    }

}
