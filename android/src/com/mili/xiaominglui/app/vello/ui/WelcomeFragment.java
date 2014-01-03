package com.mili.xiaominglui.app.vello.ui;

import com.actionbarsherlock.app.SherlockFragment;
import com.mili.xiaominglui.app.vello.R;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public final class WelcomeFragment extends SherlockFragment {
    private static final String KEY_CONTENT = "TestFragment:Content";

    public static WelcomeFragment newInstance(String content) {
        WelcomeFragment fragment = new WelcomeFragment();
        fragment.mContent = content;
        return fragment;
    }

    private String mContent = "???";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
            mContent = savedInstanceState.getString(KEY_CONTENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TextView text = new TextView(getActivity());
        text.setGravity(Gravity.CENTER);
        text.setText(mContent);
        text.setTextSize(20 * getResources().getDisplayMetrics().density);
        text.setPadding(20, 20, 20, 20);

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        layout.setGravity(Gravity.CENTER);
        if (mContent.startsWith("Discover")) {
        	layout.setBackgroundResource(R.drawable.discover);
        } else if (mContent.startsWith("Recall")) {
        	layout.setBackgroundResource(R.drawable.recall);
        } else {
        	layout.setBackgroundResource(R.drawable.acquire);
        }
        layout.addView(text);

        return layout;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CONTENT, mContent);
    }
}
