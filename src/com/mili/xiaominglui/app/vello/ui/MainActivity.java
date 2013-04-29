package com.mili.xiaominglui.app.vello.ui;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.R.layout;
import com.mili.xiaominglui.app.vello.R.menu;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	
	if (isFinishing()) {
	    return;
	}
	setContentView(R.layout.activity_main);
    }

}
