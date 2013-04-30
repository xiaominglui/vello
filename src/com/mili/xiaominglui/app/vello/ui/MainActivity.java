package com.mili.xiaominglui.app.vello.ui;

import android.os.Bundle;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.util.AccountUtils;

public class MainActivity extends BaseActivity {
    private Menu mOptionsMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	
	if (isFinishing()) {
	    return;
	}
	setContentView(R.layout.activity_main);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	super.onCreateOptionsMenu(menu);
	mOptionsMenu = menu;
	getSupportMenuInflater().inflate(R.menu.home, menu);
	//setupSearchMenuItem(menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case R.id.menu_about:
	    return true;
	case R.id.menu_sign_out:
	    AccountUtils.signOut(this);
	    finish();
	    return true;
	}
        return super.onOptionsItemSelected(item);
    }
    
}
