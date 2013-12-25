package com.mili.xiaominglui.app.vello.view;

import it.gmariotti.cardslib.library.internal.Card;
import android.content.Context;
import android.util.Log;
import android.view.View.OnTouchListener;

public class FloatCard extends Card {
	
	/**
     * Used to enable a onTouch Action on Floatcard
     */
    protected boolean mIsTouchable = false;
    
    /**
     * Listener invoked when the card is clicked
     */
    protected OnTouchListener mOnTouchListener;

	public FloatCard(Context context) {
		super(context);
	}
	
	/**
     * Returns <code>true</code> if the card is touchable
     * If card hasn't a {@link OnCardTouchListener} return <code>false</code> in any cases.
     *
     * @return
     */
    public boolean isTouchable() {
        if (mIsTouchable) {
            if (mOnTouchListener == null) {
                Log.w(TAG, "Touchable set to true without onTouchListener");
                return false;
            }
        }
        return mIsTouchable;
    }
	
	/**
     * Set the card as touchable
     *
     * @param isTouchable
     */
    public void setTouchable(boolean isTouchable) {
    	mIsTouchable = isTouchable;
    }
    
    /**
     * Returns listener invoked when card is touched
     *
     * @return listener
     */
    public OnTouchListener getOnTouchListener() {
        return mOnTouchListener;
    }
    
    /**
     * Sets listener invoked when card is touched
     * If listener is <code>null</code> then card is not touchable.
     *
     * @param onClickListener listener
     */
    public void setOnTouchListener(OnTouchListener onTouchListener) {
        if (onTouchListener != null)
            mIsClickable = true;
        else
            mIsClickable = false;
        mOnTouchListener = onTouchListener;
    }
}
