package com.mili.xiaominglui.app.vello.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.atermenji.android.iconictextview.IconicTextView;
import com.atermenji.android.iconictextview.icon.FontAwesomeIcon;
import com.haarman.listviewanimations.ArrayAdapter;
import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.data.model.Definition;
import com.mili.xiaominglui.app.vello.data.model.Definitions;
import com.mili.xiaominglui.app.vello.data.model.IcibaWord;
import com.mili.xiaominglui.app.vello.data.model.Phonetics;
import com.mili.xiaominglui.app.vello.data.model.Phoneticss;
import com.mili.xiaominglui.app.vello.data.model.Word;
import com.mili.xiaominglui.app.vello.util.AccountUtils;

public class GoogleCardsAdapter extends ArrayAdapter<Word> {

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
		final IcibaWord word = (IcibaWord) getItem(position);
		ViewHolder viewHolder;
		View view = convertView;
		Phoneticss p;
		Definitions d;
		int positionList = AccountUtils.getVocabularyListPosition(mContext,
				word.idList);
		if (view == null) {
			view = LayoutInflater.from(mContext).inflate(
					R.layout.activity_googlecards_card, parent, false);

			viewHolder = new ViewHolder();
			viewHolder.iconicToggleButton = (IconicTextView) view
					.findViewById(R.id.expandable_toggle_button);
			viewHolder.textViewKeyword = (TextView) view
					.findViewById(R.id.keyword);
			viewHolder.phonetic_area = (LinearLayout) view
					.findViewById(R.id.phonetics_area);
			viewHolder.iconicLifeCount = (IconicTextView) view
					.findViewById(R.id.life_sign);
			viewHolder.textViewLifeCount = (TextView) view
					.findViewById(R.id.life_count);

			viewHolder.definition_area = (LinearLayout) view
					.findViewById(R.id.definition_area);
			view.setTag(viewHolder);

		} else {
			viewHolder = (ViewHolder) view.getTag();
		}

		viewHolder.iconicToggleButton.setIcon(FontAwesomeIcon.INFO_SIGN);
		viewHolder.iconicToggleButton.setTextColor(Color.GRAY);
		viewHolder.iconicToggleButton.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				word.hasExpanded = true;
				return false;
			}
		});

		viewHolder.textViewKeyword.setText(getItem(position).keyword);

		viewHolder.iconicLifeCount.setIcon(FontAwesomeIcon.HEART);
		viewHolder.iconicLifeCount.setTextColor(Color.GRAY);
		viewHolder.textViewLifeCount.setText(String.valueOf(8 - positionList));
		viewHolder.textViewLifeCount.setTextColor(Color.GRAY);

		p = word.phonetics;
		viewHolder.phonetic_area.removeAllViews();
		for (Phonetics phonetics : p) {
			View phoneticsView = LayoutInflater.from(mContext).inflate(
					R.layout.phonetics_item, null);
			LinearLayout phoneticsGroup = (LinearLayout) phoneticsView
					.findViewById(R.id.phonetics_group);
			((TextView) phoneticsView.findViewById(R.id.phonetics_symbol))
					.setText("[" + phonetics.symbol + "]");
			((IconicTextView) phoneticsView.findViewById(R.id.phonetics_sound))
					.setIcon(FontAwesomeIcon.VOLUME_UP);
			viewHolder.phonetic_area.addView(phoneticsGroup);
		}

		d = word.definition;
		viewHolder.definition_area.removeAllViews();
		for (Definition definition : d) {
			View definitionView = LayoutInflater.from(mContext).inflate(
					R.layout.definition_item, null);
			LinearLayout definiitionGroup = (LinearLayout) definitionView
					.findViewById(R.id.definition_group);
			((TextView) definitionView.findViewById(R.id.pos))
					.setText(definition.pos);
			((TextView) definitionView.findViewById(R.id.definiens))
					.setText(definition.definiens);
			viewHolder.definition_area.addView(definiitionGroup);
		}
		return view;
	}

	// private void setImageView(ViewHolder viewHolder, int position) {
	// int imageResId;
	// switch (getItem(position) % 5) {
	// case 0:
	// imageResId = R.drawable.img_nature1;
	// break;
	// case 1:
	// imageResId = R.drawable.img_nature2;
	// break;
	// case 2:
	// imageResId = R.drawable.img_nature3;
	// break;
	// case 3:
	// imageResId = R.drawable.img_nature4;
	// break;
	// default:
	// imageResId = R.drawable.img_nature5;
	// }
	//
	// Bitmap bitmap = getBitmapFromMemCache(imageResId);
	// if (bitmap == null) {
	// bitmap = BitmapFactory.decodeResource(mContext.getResources(),
	// imageResId);
	// addBitmapToMemoryCache(imageResId, bitmap);
	// }
	// viewHolder.imageView.setImageBitmap(bitmap);
	// }

	// private void addBitmapToMemoryCache(int key, Bitmap bitmap) {
	// if (getBitmapFromMemCache(key) == null) {
	// mMemoryCache.put(key, bitmap);
	// }
	// }
	//
	// private Bitmap getBitmapFromMemCache(int key) {
	// return mMemoryCache.get(key);
	// }

	private static class ViewHolder {
		IconicTextView iconicToggleButton;
		TextView textViewKeyword;
		LinearLayout phonetic_area;

		IconicTextView iconicLifeCount;
		TextView textViewLifeCount;

		LinearLayout definition_area;
	}
}