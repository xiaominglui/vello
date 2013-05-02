package com.mili.xiaominglui.app.vello.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.haarman.listviewanimations.ArrayAdapter;
import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.data.model.WordCard;

public class GoogleCardsAdapter extends ArrayAdapter<WordCard> {

    private Context mContext;
    private LruCache<Integer, Bitmap> mMemoryCache;

    public GoogleCardsAdapter(Context context) {
	mContext = context;

	final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

	// Use 1/8th of the available memory for this memory cache.
	final int cacheSize = maxMemory;
	mMemoryCache = new LruCache<Integer, Bitmap>(cacheSize) {
	    @Override
	    protected int sizeOf(Integer key, Bitmap bitmap) {
		// The cache size will be measured in kilobytes rather than
		// number of items.
		return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
	    }
	};
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
	ViewHolder viewHolder;
	View view = convertView;
	if (view == null) {
	    view = LayoutInflater.from(mContext).inflate(
		    R.layout.activity_googlecards_card, parent, false);

	    viewHolder = new ViewHolder();
	    viewHolder.textView = (TextView) view
		    .findViewById(R.id.activity_googlecards_card_textview);
	    view.setTag(viewHolder);

//	    viewHolder.imageView = (ImageView) view
//		    .findViewById(R.id.activity_googlecards_card_imageview);
	} else {
	    viewHolder = (ViewHolder) view.getTag();
	}

	viewHolder.textView.setText(getItem(position).name);
//	setImageView(viewHolder, position);

	return view;
    }

//    private void setImageView(ViewHolder viewHolder, int position) {
//	int imageResId;
//	switch (getItem(position) % 5) {
//	case 0:
//	    imageResId = R.drawable.img_nature1;
//	    break;
//	case 1:
//	    imageResId = R.drawable.img_nature2;
//	    break;
//	case 2:
//	    imageResId = R.drawable.img_nature3;
//	    break;
//	case 3:
//	    imageResId = R.drawable.img_nature4;
//	    break;
//	default:
//	    imageResId = R.drawable.img_nature5;
//	}
//
//	Bitmap bitmap = getBitmapFromMemCache(imageResId);
//	if (bitmap == null) {
//	    bitmap = BitmapFactory.decodeResource(mContext.getResources(),
//		    imageResId);
//	    addBitmapToMemoryCache(imageResId, bitmap);
//	}
//	viewHolder.imageView.setImageBitmap(bitmap);
//    }

//    private void addBitmapToMemoryCache(int key, Bitmap bitmap) {
//	if (getBitmapFromMemCache(key) == null) {
//	    mMemoryCache.put(key, bitmap);
//	}
//    }
//
//    private Bitmap getBitmapFromMemCache(int key) {
//	return mMemoryCache.get(key);
//    }

    private static class ViewHolder {
	TextView textView;
	ImageView imageView;
    }
}