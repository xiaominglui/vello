package com.mili.xiaominglui.app.vello.ui;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.view.CardView;

import java.util.Locale;

import wei.mark.standout.StandOutWindow;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.service.VelloService;

public class FloatDictCardWindow extends StandOutWindow {
	private static final String TAG = FloatDictCardWindow.class.getSimpleName();
	
	@Override
	public String getAppName() {
		// TODO Auto-generated method stub
		return "VAA FloatDictCard";
	}

	@Override
	public int getAppIcon() {
		return 0;
	}

	@Override
	public void createAndAttachView(int id, FrameLayout frame) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.float_dict_card, frame, true);
	}

	@Override
	public StandOutLayoutParams getParams(int id, Window window) {
		return new StandOutLayoutParams(id, 250, 300, StandOutLayoutParams.CENTER, StandOutLayoutParams.TOP);
	}
	
	@Override
	public int getFlags(int id) {
		return 
				StandOutFlags.FLAG_WINDOW_FOCUSABLE_DISABLE |
				StandOutFlags.FLAG_WINDOW_HIDE_ENABLE;
	}
	
	@Override
	public void onReceiveData(int id, int requestCode, Bundle data,
			Class<? extends StandOutWindow> fromCls, int fromId) {
		switch (requestCode) {
		case VelloService.REQ_DATA_CHANGED:
			Window window = getWindow(id);
			if (window == null) {
				String errorText = String.format(Locale.US,
						"%s received data but Window id: %d is not open.",
						getAppName(), id);
				Toast.makeText(this, errorText, Toast.LENGTH_SHORT).show();
				return;
			}
			String changedText = data.getString("changedText");
			//Create a Card
		    Card card = new Card(getApplicationContext());

		    //Create a CardHeader
		    CardHeader header = new CardHeader(getApplicationContext());
			header.setTitle(changedText);
			card.addCardHeader(header);
			CardView cv = (CardView) window.findViewById(R.id.float_dict_card);
			cv.setCard(card);
			break;
		default:
			Log.d(TAG, "Unexpected data received.");
			break;
		}
	}
}
