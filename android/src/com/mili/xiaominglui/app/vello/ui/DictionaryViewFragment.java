package com.mili.xiaominglui.app.vello.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.mili.xiaominglui.app.vello.R;

public class DictionaryViewFragment extends SherlockFragment {
	private static final String TAG = DictionaryViewFragment.class
			.getSimpleName();
	
	private ViewGroup mRootView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_dictionary, null);
		TextView tv = (TextView) mRootView.findViewById(R.id.keyword);
		tv.setText("fjlsjflsjflsjflsjflsjflsfs");
		return super.onCreateView(inflater, container, savedInstanceState);
	}

}
