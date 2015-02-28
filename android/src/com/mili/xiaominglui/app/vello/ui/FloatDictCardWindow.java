package com.mili.xiaominglui.app.vello.ui;

import it.gmariotti.cardslib.library.view.CardView;

import java.util.Locale;

import wei.mark.standout.StandOutWindow;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.card.FloatDictCard;
import com.mili.xiaominglui.app.vello.data.factory.MiliDictionaryJsonParser;
import com.mili.xiaominglui.app.vello.data.model.Definition;
import com.mili.xiaominglui.app.vello.data.model.IcibaWord;
import com.mili.xiaominglui.app.vello.data.model.TrelloCard;
import com.mili.xiaominglui.app.vello.service.VelloService;

public class FloatDictCardWindow extends StandOutWindow {
	private static final String TAG = FloatDictCardWindow.class.getSimpleName();

	private FloatDictCard mCard;

	@Override
	public String getAppName() {
		return "VAA Lookup Copied";
	}

	@Override
	public int getAppIcon() {
		return R.drawable.ic_stat_vaa;
	}

	@Override
	public void createAndAttachView(int id, FrameLayout frame) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.float_dict_card, frame, true);
		mCard = new FloatDictCard(getApplicationContext());
		CardView cv = (CardView) rootView.findViewById(R.id.float_dict_card);
		cv.setCard(mCard);
	}

	@Override
	public StandOutLayoutParams getParams(int id, Window window) {
		return new StandOutLayoutParams(id, StandOutLayoutParams.MATCH_PARENT, StandOutLayoutParams.WRAP_CONTENT, StandOutLayoutParams.LEFT, StandOutLayoutParams.TOP);
	}
	
	@Override
	public int getFlags(int id) {
		return 
				StandOutFlags.FLAG_WINDOW_FOCUSABLE_DISABLE |
				StandOutFlags.FLAG_WINDOW_HIDE_ENABLE;
	}
	
	@Override
	public Animation getShowAnimation(int id) {
		return AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
	}

	@Override
	public Animation getCloseAnimation(int id) {
		return AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
	}

	@Override
	public void onReceiveData(int id, int requestCode, Bundle data, Class<? extends StandOutWindow> fromCls, int fromId) {
		switch (requestCode) {
		case VelloService.REQ_DATA_CHANGED:
			Window window = getWindow(id);
			if (window == null) {
				String errorText = String.format(Locale.US, "%s received data but Window id: %d is not open.", getAppName(), id);
				Toast.makeText(this, errorText, Toast.LENGTH_SHORT).show();
				return;
			}
			TrelloCard card = data.getParcelable("changedData");
			String jsonResponse = data.getString("jsonResponse");
			IcibaWord word = null;
			if (card == null && jsonResponse != null) {
				word = MiliDictionaryJsonParser.parse(jsonResponse);
			} else if (jsonResponse == null && card != null) {
				word = MiliDictionaryJsonParser.parse(card.desc);
			}
            mCard.setTitle(word.keyword);
			StringBuilder meaning = new StringBuilder();
			for (Definition definition : word.definition) {
				meaning.append(definition.pos);
				meaning.append(" ");
				meaning.append(definition.definiens);
				meaning.append(";");
				meaning.append("\n");
			}
			mCard.setMeanning(meaning.toString());
			CardView cv = (CardView) window.findViewById(R.id.float_dict_card);
			cv.refreshCard(mCard);
			cv.setVisibility(View.VISIBLE);
			break;
		default:
			Log.d(TAG, "Unexpected data received.");
			break;
		}
	}
}
