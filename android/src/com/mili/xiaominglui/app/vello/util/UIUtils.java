package com.mili.xiaominglui.app.vello.util;

import android.os.Build;

public class UIUtils {

	public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }
}
