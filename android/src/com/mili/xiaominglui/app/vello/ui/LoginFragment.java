package com.mili.xiaominglui.app.vello.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockFragment;
import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.adapter.WelcomeFragmentAdapter;
import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.UnderlinePageIndicator;

public class LoginFragment extends SherlockFragment implements View.OnClickListener {
	onButtonClickedListener mListener;
	WelcomeFragmentAdapter mAdapter;
    ViewPager mPager;
    PageIndicator mIndicator;

    Button mLogInButton;
	public LoginFragment() {
	}
	
	public interface onButtonClickedListener {
		public void onSignInButtonClicked();
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
            mListener = (onButtonClickedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement onButtonClickedListener");
        }
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_login, container, false);
		
		mAdapter = new WelcomeFragmentAdapter(getActivity().getSupportFragmentManager());

        mPager = (ViewPager) rootView.findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        mIndicator = (UnderlinePageIndicator) rootView.findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);

        mLogInButton = (Button) rootView.findViewById(R.id.log_in);
        mLogInButton.setOnClickListener(this);
		return rootView;
	}

	@Override
	public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.log_in:
            	mListener.onSignInButtonClicked();
                break;
        }
	}
}
