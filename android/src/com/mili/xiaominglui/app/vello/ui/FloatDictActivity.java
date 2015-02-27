package com.mili.xiaominglui.app.vello.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.mili.xiaominglui.app.vello.R;
import com.mili.xiaominglui.app.vello.base.C;
import com.mili.xiaominglui.app.vello.base.log.L;
import com.mili.xiaominglui.app.vello.service.VelloService;

import java.util.Locale;
public class FloatDictActivity extends BaseActivity {
    private static final String TAG = FloatDictActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L.d(TAG, "onCreate");
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            }
        } else {
            Toast.makeText(C.get(), getText(R.string.toast_invalid_keyword), Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        L.d(TAG, "onStop");
    }

    private void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            String cleanedKeyword = sharedText.trim().toLowerCase(Locale.US);
            if (cleanedKeyword.matches("^[a-z]+$")) {
                // Update UI to reflect text being shared
                Intent startShareLookup = new Intent(getApplicationContext(), VelloService.class);
                startShareLookup.putExtra("share", true);
                startShareLookup.putExtra("keyword", cleanedKeyword);
                ComponentName service = getApplicationContext().startService(startShareLookup);
                if (service == null) {
                    L.e(TAG, "Can't start service " + VelloService.class.getName());
                }
            }
        }
    }
}
