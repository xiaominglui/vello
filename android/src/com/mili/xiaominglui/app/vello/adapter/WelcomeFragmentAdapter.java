package com.mili.xiaominglui.app.vello.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.mili.xiaominglui.app.vello.ui.WelcomeFragment;

public class WelcomeFragmentAdapter extends FragmentPagerAdapter {


    public WelcomeFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return WelcomeFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return 3;
    }
}