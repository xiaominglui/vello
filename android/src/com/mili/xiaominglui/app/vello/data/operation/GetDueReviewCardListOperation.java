package com.mili.xiaominglui.app.vello.data.operation;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import com.foxykeep.datadroid.exception.ConnectionException;
import com.foxykeep.datadroid.exception.CustomRequestException;
import com.foxykeep.datadroid.exception.DataException;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.service.RequestService.Operation;
import com.mili.xiaominglui.app.vello.data.model.DirtyCard;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent.DbWordCard;
import com.mili.xiaominglui.app.vello.data.provider.util.ProviderCriteria;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;

public class GetDueReviewCardListOperation implements Operation {

	@Override
	public Bundle execute(Context context, Request request) throws ConnectionException, DataException, CustomRequestException {
		final ContentResolver resolver = context.getContentResolver();
		// query and backup all local dirty items(dateLastOperation != "") firstly
		// and merge locally later(because we has only Trello APIs but admin right)
		ArrayList<DirtyCard> localDirtyCards = new ArrayList<DirtyCard>();
		ProviderCriteria criteria = new ProviderCriteria();
		criteria.addNe(DbWordCard.Columns.DATE_LAST_OPERATION, "");
		Cursor c = resolver.query(DbWordCard.CONTENT_URI, DbWordCard.PROJECTION, criteria.getWhereClause(), criteria.getWhereParams(), criteria.getOrderClause());
		if (c != null) {
			while (c.moveToNext()) {
				DirtyCard dc = new DirtyCard(c);
				localDirtyCards.add(dc);
			}
		}
		Bundle bundle = new Bundle();
		bundle.putParcelableArrayList(VelloRequestFactory.BUNDLE_EXTRA_DIRTY_CARD_LIST, localDirtyCards);
		return bundle;
	}
}
