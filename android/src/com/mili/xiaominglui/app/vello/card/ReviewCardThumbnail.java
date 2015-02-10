package com.mili.xiaominglui.app.vello.card;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mili.xiaominglui.app.vello.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import it.gmariotti.cardslib.library.internal.CardThumbnail;

public class ReviewCardThumbnail extends CardThumbnail {
    private String mImageUrl;
    DisplayImageOptions mOptions;
    public ReviewCardThumbnail(Context context, String imageUrl) {
        super(context);
        mImageUrl = imageUrl;
        mOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.word_image_default_big)
                .showImageForEmptyUri(R.drawable.word_image_default_big)
                .showImageOnFail(R.drawable.word_image_default_big)
                .cacheInMemory(true)
                .build();
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View imageView) {
        ImageLoader imageLoader = ImageLoader.getInstance();

        imageLoader.displayImage(mImageUrl, (ImageView) imageView, mOptions);
    }
}
