package com.mili.xiaominglui.app.vello.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class WordsDeleteConfirmationDialog extends DialogFragment {
	private DialogInterface.OnClickListener callback;
	public static WordsDeleteConfirmationDialog newInstance(String msg) {
		WordsDeleteConfirmationDialog frag = new WordsDeleteConfirmationDialog();
		
		Bundle args = new Bundle();
		args.putString("msg", msg);
        frag.setArguments(args);
        return frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
            callback = (DialogInterface.OnClickListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement DialogClickListener interface");
        }
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String msg = getArguments().getString("msg");
		Resources res = getResources();
		return new AlertDialog.Builder(getActivity())
				.setCancelable(true)
				.setMessage(msg)
				.setPositiveButton(res.getString(android.R.string.ok), callback)
				.setNegativeButton(res.getString(android.R.string.cancel), callback).create();
	}
}
