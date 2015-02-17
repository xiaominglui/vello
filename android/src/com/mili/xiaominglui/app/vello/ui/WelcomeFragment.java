package com.mili.xiaominglui.app.vello.ui;

import com.actionbarsherlock.app.SherlockFragment;
import com.mili.xiaominglui.app.vello.R;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public final class WelcomeFragment extends SherlockFragment {
    private static final String KEY_POSITION = "WelcomeFragment:Position";

    public static WelcomeFragment newInstance(int position) {
        WelcomeFragment fragment = new WelcomeFragment();
        fragment.mPosition = position;
        return fragment;
    }

    private int mPosition = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_POSITION)) {
            mPosition = savedInstanceState.getInt(KEY_POSITION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_welcome, null);
        ImageView img = (ImageView) root.findViewById(R.id.feature_img);
        TextView desc = (TextView) root.findViewById(R.id.feature_desc);
        final Drawable drawableImg;
        final String titleDesc;
        switch (mPosition) {
            case 0:
                drawableImg = getResources().getDrawable(R.drawable.img_feature_one);
                titleDesc = getString(R.string.title_feature_one);
                break;
            case 1:
                drawableImg = getResources().getDrawable(R.drawable.img_feature_two);
                titleDesc = getString(R.string.title_feature_two);
                break;
            case 2:
                drawableImg = getResources().getDrawable(R.drawable.img_feature_three);
                titleDesc = getString(R.string.title_feature_three);
                break;
            default:
                drawableImg = null;
                titleDesc = "";
        }
        img.setImageDrawable(drawableImg);
        desc.setText(titleDesc);
        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_POSITION, mPosition);
    }
}
