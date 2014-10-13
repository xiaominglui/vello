package com.mili.xiaominglui.app.vello.fragment;

import android.view.View;
import android.view.animation.AnimationUtils;

import com.mili.xiaominglui.app.vello.R;

/**
 * Created by xiaominglui on 14-10-14.
 */
public abstract class BaseListFragment extends BaseFragment {
    protected boolean mListShown;
    protected View mProgressContainer;
    protected View mListContainer;

    /**
     * Setup the list fragment
     *
     * @param root
     */
    protected void setupListFragment(View root) {

        mListContainer = root.findViewById(R.id.listContainer);
        mProgressContainer = root.findViewById(R.id.progressContainer);
        mListShown = true;
    }

    protected void displayList(){
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
    }

    protected void hideList(boolean animate){
        setListShown(false,animate);
    }

    /**
     * @param shown
     * @param animate
     */
    protected void setListShown(boolean shown, boolean animate) {
        if (mListShown == shown) {
            return;
        }
        mListShown = shown;
        if (shown) {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_out));
                mListContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_in));
            }
            mProgressContainer.setVisibility(View.GONE);
            mListContainer.setVisibility(View.VISIBLE);
        } else {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_in));
                mListContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_out));
            }
            mProgressContainer.setVisibility(View.VISIBLE);
            mListContainer.setVisibility(View.INVISIBLE);
        }
    }

    public void setListShown(boolean shown) {
        setListShown(shown, true);
    }

    public void setListShownNoAnimation(boolean shown) {
        setListShown(shown, false);
    }
}
