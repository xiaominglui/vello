package com.mili.xiaominglui.app.vello.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.data.provider.VelloContent;
import com.mili.xiaominglui.app.vello.dialogs.ErrorDialogFragment.ErrorDialogFragmentBuilder;
import com.mili.xiaominglui.app.vello.util.AccountUtils;

public class BaseActivity extends SherlockFragmentActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (!AccountUtils.isAuthenticated(this)) {
            AccountUtils.startAuthenticationFlow(this, getIntent());
            finish();
        }
        

    }

    protected void showBadDataErrorDialog() {
        new ErrorDialogFragmentBuilder(this).setTitle(R.string.dialog_error_data_error_title).setMessage(R.string.dialog_error_data_error_message).show();
    }
}
