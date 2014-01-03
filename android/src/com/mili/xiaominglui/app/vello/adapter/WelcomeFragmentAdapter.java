package com.mili.xiaominglui.app.vello.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.mili.xiaominglui.app.vello.ui.WelcomeFragment;

public class WelcomeFragmentAdapter extends FragmentPagerAdapter {
    protected static final String[] CONTENT = new String[] { "Discover new words while reading", "Recall unfamiliar words after reading", "Acquire more words via reading"};

    private int mCount = CONTENT.length;

    public WelcomeFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return WelcomeFragment.newInstance(CONTENT[position % CONTENT.length]);
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public CharSequence getPageTitle(int position) {
      return WelcomeFragmentAdapter.CONTENT[position % CONTENT.length];
    }

    public void setCount(int count) {
        if (count > 0 && count <= 10) {
            mCount = count;
            notifyDataSetChanged();
        }
    }
}