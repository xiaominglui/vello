package com.mili.xiaominglui.app.vello.data.factory;

import java.io.IOException;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbDictCard;
import com.mili.xiaominglui.app.vello.ui.DictCard;
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
        DictCard[] dictCards = new Gson().fromJson(json, DictCard[].class);
        int noOfDictCards = dictCards.length;
        for (int i = 0; i < noOfDictCards; i++) {
            parseDictCard(dictCards[i], batch);
        }
        return batch;
    }

    private static void parseDictCard(DictCard dictCard, ArrayList<ContentProviderOperation> batch) {
        ContentProviderOperation.Builder builder = ContentProviderOperation
                .newInsert(DbDictCard.CONTENT_URI);
        builder.withValues(dictCard.toContentValues());
        batch.add(builder.build());
    }

}
