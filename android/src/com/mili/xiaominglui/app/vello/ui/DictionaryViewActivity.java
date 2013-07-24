package com.mili.xiaominglui.app.vello.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.mili.xiaominglui.app.vello.R;

public class DictionaryViewActivity extends BaseActivity {
	private TestFragment mTestFragement;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		FragmentManager fm = getSupportFragmentManager();
		mTestFragement = (TestFragment) fm.findFragmentById(R.id.fragment_container_master);
		if (mTestFragement == null) {
			mTestFragement = new TestFragment();
			fm.beginTransaction().add(R.id.fragment_container_master, mTestFragement).commit();
		}
		
	}

}
