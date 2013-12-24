package com.mili.xiaominglui.app.vello.ui;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.atermenji.android.iconictextview.IconicTextView;
import com.atermenji.android.iconictextview.icon.FontAwesomeIcon;
import com.mili.xiaominglui.app.vello.R;

public class ConnectionTimeOutFragment extends SherlockFragment {
	private ConnectionTimeOutFragmentEventListener mListener;
	
	public interface ConnectionTimeOutFragmentEventListener {
		public void onReload();
	}
	
	public static Fragment newInstance() {
		Fragment f = new ConnectionTimeOutFragment();
		return f;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (ConnectionTimeOutFragmentEventListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement ConnectionTimeOutFragmentEventListener");
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_connection_timeout, container, false);
		rootView.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mListener.onReload();
				return true;
			}
		});
		IconicTextView iconicTextView = (IconicTextView) rootView.findViewById(R.id.iconic_connection_timeout);
		iconicTextView.setIcon(FontAwesomeIcon.INFO_SIGN);
		iconicTextView.setTextColor(Color.LTGRAY);
		return rootView;
	}
}
