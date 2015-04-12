package com.mili.xiaominglui.app.vello.ui;

import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.base.C;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class WelcomeFragment extends Fragment {
    private static final String KEY_POSITION = "WelcomeFragment:Position";
    private LinearLayout mFeatureGallery;

    public static WelcomeFragment newInstance(int position) {
        WelcomeFragment fragment = new WelcomeFragment();
        fragment.mPosition = position;
        return fragment;
    }

    private int mPosition = 0;

    private int[] mImageIdsOne = new int[]{
            R.drawable.dk_android_recall_list,
            R.drawable.dk_android_recall_list
    };

    private int[] mImageIdsTwo = new int[]{
            R.drawable.dk_lookup_share_float_card,
            R.drawable.dk_lookup_share_float_card,
            R.drawable.dk_android_recall_list
    };

    private int[] mImageIdsThree = new int[]{
            R.drawable.dk_lookup_share_float_card,
            R.drawable.dk_lookup_share_float_card,
            R.drawable.dk_android_recall_list
    };



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
        mFeatureGallery = (LinearLayout) root.findViewById(R.id.feature_imgs);

        final String titleDesc;
        switch (mPosition) {
            case 0:
                titleDesc = getString(R.string.title_feature_one);
                break;
            case 1:
                titleDesc = getString(R.string.title_feature_two);
                break;
            case 2:
                titleDesc = getString(R.string.title_feature_three);
                break;
            default:
                titleDesc = "";
        }
        insertImages(mPosition);
        desc.setText(titleDesc);
        return root;
    }

    private void insertImages(int position) {
        mFeatureGallery.removeAllViews();
        View v = null;
        switch (position) {
            case 0:
                v = LayoutInflater.from(C.get()).inflate(R.layout.feature_gallery_item_lookup, mFeatureGallery, true);
                break;
            case 1:
                v = LayoutInflater.from(C.get()).inflate(R.layout.feature_gallery_item_review, mFeatureGallery, true);
                break;
            case 2:
                v = LayoutInflater.from(C.get()).inflate(R.layout.feature_gallery_item_final, mFeatureGallery, true);
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_POSITION, mPosition);
    }
}
