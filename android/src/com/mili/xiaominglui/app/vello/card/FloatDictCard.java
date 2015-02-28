package com.mili.xiaominglui.app.vello.card;

import it.gmariotti.cardslib.library.internal.Card;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mili.xiaominglui.app.vello.R;

public class FloatDictCard extends Card {
    protected String mTitle;
    protected String mMeaning;

    public FloatDictCard(Context context) {
        super(context, R.layout.float_dict_card_inner_content);
        init();
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setMeanning(String meaning) {
        mMeaning = meaning;
    }

    public void init() {
        setClickable(false);
        setLongClickable(false);
        setSwipeable(false);
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        if (view != null) {
            TextView mTitleView = (TextView) view.findViewById(R.id.float_card_inner_title);
            if (mTitleView != null) {
                mTitleView.setText(mTitle);
            }
            TextView mMeaningView = (TextView) view.findViewById(R.id.float_card_inner_meaning);
            if (mMeaningView != null) {
                mMeaningView.setText(mMeaning);
            }
        }
    }
}
