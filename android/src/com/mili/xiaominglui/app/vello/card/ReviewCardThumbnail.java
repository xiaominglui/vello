package com.mili.xiaominglui.app.vello.card;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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
                .cacheInMemory(true)
                .build();
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View imageView) {
        ImageLoader imageLoader = ImageLoader.getInstance();

        if (!TextUtils.isEmpty(mImageUrl)) {
            imageLoader.displayImage(mImageUrl, (ImageView) imageView, mOptions);
        } else {

        }

//        imageView.getLayoutParams().width = 128;
//        imageView.getLayoutParams().height = 128;

    }
}
