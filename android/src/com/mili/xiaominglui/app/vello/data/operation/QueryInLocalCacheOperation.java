package com.mili.xiaominglui.app.vello.data.operation;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import com.foxykeep.datadroid.exception.ConnectionException;
import com.foxykeep.datadroid.exception.CustomRequestException;
import com.foxykeep.datadroid.exception.DataException;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.service.RequestService.Operation;
import com.mili.xiaominglui.app.vello.data.model.TrelloCard;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbWordCard;
import com.mili.xiaominglui.app.vello.data.provider.util.ProviderCriteria;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;

public class QueryInLocalCacheOperation implements Operation {

    @Override
    public Bundle execute(Context context, Request request) throws ConnectionException,
            DataException, CustomRequestException {
        Bundle bundle = new Bundle();
        String query = request.getString(VelloRequestFactory.PARAM_EXTRA_QUERY_WORD_KEYWORD);
        ProviderCriteria criteria = new ProviderCriteria();
        criteria.addSortOrder(DbWordCard.Columns.DUE, true);
        criteria.addEq(DbWordCard.Columns.NAME, query);
        Cursor c = context.getContentResolver().query(DbWordCard.CONTENT_URI, DbWordCard.PROJECTION, criteria.getWhereClause(), criteria.getWhereParams(), criteria.getOrderClause());
        if (c != null) {
            if (c.getCount() == 1 && c.moveToFirst()) {
                TrelloCard wordcard = new TrelloCard(c);
                bundle.putParcelable(VelloRequestFactory.BUNDLE_EXTRA_WORDCARD, wordcard);
            }
        }
        return bundle;
    }
}
