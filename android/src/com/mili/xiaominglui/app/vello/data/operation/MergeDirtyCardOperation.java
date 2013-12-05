package com.mili.xiaominglui.app.vello.data.operation;

import android.content.Context;
import android.os.Bundle;

import com.foxykeep.datadroid.exception.ConnectionException;
import com.foxykeep.datadroid.exception.CustomRequestException;
import com.foxykeep.datadroid.exception.DataException;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.service.RequestService.Operation;
import com.mili.xiaominglui.app.vello.config.WSConfig;
import com.mili.xiaominglui.app.vello.data.model.DirtyCard;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestFactory;
import com.mili.xiaominglui.app.vello.util.AccountUtils;

public class MergeDirtyCardOperation implements Operation {

	@Override
	public Bundle execute(Context context, Request request)
			throws ConnectionException, DataException, CustomRequestException {
		// TODO Auto-generated method stub
		String token = AccountUtils.getAuthToken(context);
		DirtyCard dirtyCard = (DirtyCard) request.getParcelable(VelloRequestFactory.PARAM_EXTRA_DIRTY_CARD);
		String urlString;
		if (dirtyCard.markDeleted.equals("true")) {
			// to delete
			urlString = WSConfig.TRELLO_API_URL + WSConfig.WS_TRELLO_TARGET_CARD + "/" + dirtyCard.id;
			
		}
		return null;
	}

}
