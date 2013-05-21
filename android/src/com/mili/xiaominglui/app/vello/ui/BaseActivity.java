package com.mili.xiaominglui.app.vello.ui;

import java.util.ArrayList;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.foxykeep.datadroid.requestmanager.Request;
import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.data.requestmanager.VelloRequestManager;
import com.mili.xiaominglui.app.vello.dialogs.ErrorDialogFragment.ErrorDialogFragmentBuilder;
import com.mili.xiaominglui.app.vello.util.AccountUtils;

public class BaseActivity extends SherlockFragmentActivity {
    private static final String SAVED_STATE_REQUEST_LIST = "savedStateRequestList";
    
    protected VelloRequestManager mRequestManager;
    protected ArrayList<Request> mRequestList;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	setTheme(R.style.Theme_Sherlock_Light_DarkActionBar);
        super.onCreate(savedInstanceState);
        
        if (!AccountUtils.isAuthenticated(this)) {
            AccountUtils.startAuthenticationFlow(this, getIntent());
            finish();
        }
        
        mRequestManager = VelloRequestManager.from(this);
        if (savedInstanceState != null) {
            mRequestList = savedInstanceState.getParcelableArrayList(SAVED_STATE_REQUEST_LIST);
        } else {
            mRequestList = new ArrayList<Request>();
        }
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(SAVED_STATE_REQUEST_LIST, mRequestList);
    }

    protected void showBadDataErrorDialog() {
        new ErrorDialogFragmentBuilder(this).setTitle(R.string.dialog_error_data_error_title)
                .setMessage(R.string.dialog_error_data_error_message).show();
    }
}
