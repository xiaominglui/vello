package com.mili.xiaominglui.app.vello.util;

import android.accounts.Account;

import com.mili.xiaominglui.app.vello.authenticator.Constants;
import com.mili.xiaominglui.app.vello.base.C;
import com.mili.xiaominglui.app.vello.syncadapter.SyncHelper;

/**
 * Created by xml on 3/20/15.
 */
public class CommonUtils {
    public static void triggerRefresh() {
        SyncHelper.requestManualSync(new Account(AccountUtils.getAccountName(C.get()), Constants.ACCOUNT_TYPE));
    }
}
