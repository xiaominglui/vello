package com.mili.xiaominglui.app.vello.ui;

import com.mili.xiaominglui.app.vello.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public final class WelcomeFragment extends Fragment {
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
        TextView desc = (TextView) root.findViewById(R.id.feature_desc);
        ImageView sample = (ImageView) root.findViewById(R.id.img_sample);

        final String titleDesc;
        switch (mPosition) {
            case 0:
                sample.setImageDrawable(getResources().getDrawable(R.drawable.dk_lookup_share_float_card));
                titleDesc = getString(R.string.title_feature_one);
                break;
            case 1:
                sample.setImageDrawable(getResources().getDrawable(R.drawable.dk_android_recall_list));
                titleDesc = getString(R.string.title_feature_two);
                break;
            case 2:
                sample.setImageDrawable(getResources().getDrawable(R.drawable.vaa512));
                titleDesc = getString(R.string.title_feature_three);
                break;
            default:
                titleDesc = "";
        }
        desc.setText(titleDesc);
        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_POSITION, mPosition);
    }
}
