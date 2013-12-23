package com.mili.xiaominglui.app.vello.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.atermenji.android.iconictextview.IconicTextView;
import com.atermenji.android.iconictextview.icon.FontAwesomeIcon;
import com.mili.xiaominglui.app.vello.R;

public class SyncBlankViewFragment extends SherlockFragment {
	
	public static Fragment newInstance() {
		Fragment f = new SyncBlankViewFragment();
		return f;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_sync_blank, container, false);
		IconicTextView iconicTextView = (IconicTextView) rootView.findViewById(R.id.iconic_sync_all_recalled);
		iconicTextView.setIcon(FontAwesomeIcon.THUMBS_UP);
		iconicTextView.setTextColor(Color.LTGRAY);
		return rootView;
	}

}
