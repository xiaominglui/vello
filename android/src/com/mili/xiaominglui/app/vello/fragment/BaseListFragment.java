package com.mili.xiaominglui.app.vello.fragment;

import android.view.View;
import android.view.animation.AnimationUtils;

import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.base.log.L;
public abstract class BaseListFragment extends BaseFragment {
    private static final String TAG = BaseListFragment.class.getSimpleName();
    protected boolean mListShown;
    protected View mProgressContainer;
    protected View mListContainer;

    protected void setupListFragment(View root) {
        mListContainer = root.findViewById(R.id.ptr_frame);
        mProgressContainer = root.findViewById(R.id.progressContainer);
        mListShown = true;
    }

    protected void displayList(){
        L.d(TAG, "displayList");
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
    }

    protected void hideList(boolean animate){
        setListShown(false,animate);
    }

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
