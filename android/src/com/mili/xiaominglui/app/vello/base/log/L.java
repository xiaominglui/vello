package com.mili.xiaominglui.app.vello.base.log;

import android.util.Log;

public class L {
    public static final boolean VDBG = false;
    public static final boolean DDBG = true;
    public static final boolean IDBG = false;
    public static final boolean EDBG = false;
    public static final boolean WDBG = false;
    public static final boolean LDBG = false;
    public static final boolean IS_WTF = false;//是否写入sd卡文件
    public static void d(String tag, Object o) {
        if (DDBG) {
            Log.d(tag, String.valueOf(o));
        }
    }

    public static void d(Object o) {
        if (DDBG) {
            Log.d("vello", String.valueOf(o));
        }
    }

    public static void e(String tag, Object o) {
        if (EDBG) {
            Log.e(tag, String.valueOf(o));
        }
    }

    public static void i(String tag, Object o) {
        if (IDBG) {
            Log.i(tag, String.valueOf(o));
        }
    }

    public static void v(String tag, Object o) {
        if (VDBG) {
            Log.v(tag, String.valueOf(o));
        }
    }

    public static void w(String tag, Object o) {

        if (WDBG) {
            Log.w(tag, String.valueOf(o));
        }
    }


    public static void wtf(String tag,Object o,String path) {
        if (LDBG) {
            LogWritter.writeToFile(path,tag,String.valueOf(o));
        }
    }

    public static void wtf(String tag, Object o) {
        if (IS_WTF) {
            LogWritter.writeToFile(tag, String.valueOf(o));
        }
    }
    public static void wtfAnyWay(String tag, Object o) {
        LogWritter.writeToFile(tag, String.valueOf(o));
    }
    public static void wtfAnyWay(String tag, Object o, String path) {
        LogWritter.writeToFile(path, tag, String.valueOf(o));
    }

}
