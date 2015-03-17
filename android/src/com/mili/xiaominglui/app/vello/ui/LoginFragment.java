package com.mili.xiaominglui.app.vello.ui;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.adapter.WelcomeFragmentAdapter;
import com.mili.xiaominglui.app.vello.base.log.L;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.UnderlinePageIndicator;

public class LoginFragment extends Fragment implements View.OnClickListener, OnPageChangeListener {
    private static final String TAG = LoginFragment.class.getSimpleName();
    onButtonClickedListener mListener;
	WelcomeFragmentAdapter mAdapter;
    ViewPager mPager;
    PageIndicator mIndicator;

    Button mLogInButton;
    ViewGroup mRoot;
    ImageView mBackground;
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
		mRoot = (ViewGroup) inflater.inflate(R.layout.fragment_login, container, false);

        mBackground = (ImageView) mRoot.findViewById(R.id.feature_img);
        mBackground.setImageDrawable(getResources().getDrawable(R.drawable.img_feature_one));

		mAdapter = new WelcomeFragmentAdapter(getActivity().getSupportFragmentManager());

        mPager = (ViewPager) mRoot.findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        mIndicator = (UnderlinePageIndicator) mRoot.findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
        mIndicator.setOnPageChangeListener(this);

        mLogInButton = (Button) mRoot.findViewById(R.id.log_in);
        mLogInButton.setOnClickListener(this);
		return mRoot;
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

    @Override
    public void onPageSelected(int i) {
        L.d(TAG, "onPageSelected: " + i);
        final Drawable drawableImg;
        switch (i) {
            case 0:
                drawableImg = getResources().getDrawable(R.drawable.img_feature_one);
                break;
            case 1:
                drawableImg = getResources().getDrawable(R.drawable.img_feature_two);
                break;
            case 2:
                drawableImg = getResources().getDrawable(R.drawable.img_feature_three);
                break;
            default:
                drawableImg = null;
        }
        mBackground.setImageDrawable(drawableImg);

    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }
}
