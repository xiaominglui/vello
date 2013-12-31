package com.mili.xiaominglui.app.vello.ui;

import com.mili.xiaominglui.app.vello.R;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import it.gmariotti.cardslib.library.internal.CardHeader;

public class FloatDictCardHeader extends CardHeader {
	/**
     * meanning json data
     */
    protected String mMeaning;

	public FloatDictCardHeader(Context context) {
		super(context, R.layout.inner_float_card_header);
	}
	
	public void setMeanning(String meaning) {
		mMeaning = meaning;
	}
	
	@Override
	public void setupInnerViewElements(ViewGroup parent, View view) {
        if (view != null) {
        	TextView mTitleView = (TextView) view.findViewById(R.id.card_header_inner_simple_title);
            if (mTitleView != null) {
            	mTitleView.setText(mTitle);
            }
            TextView mMeaningView = (TextView) view.findViewById(R.id.float_card_header_inner_meaning);
            if (mMeaningView != null) {
            	mMeaningView.setText(mMeaning);
            }
        }
	}
}
