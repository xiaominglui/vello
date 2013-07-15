package com.mili.xiaominglui.app.vello.ui;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.aphidmobile.flip.FlipViewController;
import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.data.model.WordCard;

public class WordDetailFlipView extends SherlockFragmentActivity {
	private FlipViewController mFlipView;
	private WordCard mWord;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mFlipView = new FlipViewController(this);
		mFlipView.setAdapter(new WordDetailFlipViewAdapter(this, mFlipView));
		setContentView(mFlipView);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mFlipView.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mFlipView.onPause();
	}
	
	public class WordDetailFlipViewAdapter extends BaseAdapter {

		public WordDetailFlipViewAdapter(WordDetailFlipView wordDetailFlipView,
				FlipViewController mFlipView) {
			// TODO Auto-generated constructor stub
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
