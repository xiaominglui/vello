package com.mili.xiaominglui.app.vello.ui;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.mili.xiaominglui.app.vello.R;

public class AccountActivity extends SherlockFragmentActivity {
    private static final String TAG = AccountActivity.class.getSimpleName();
    
    public static final String EXTRA_FINISH_INTENT = "com.mili.xiaominglui.app.vello.extra.FINISH_INTENT";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_account);
    }

}
