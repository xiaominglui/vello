package com.mili.xiaominglui.app.vello.base.log;


import android.app.ActivityManager;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;

import com.mili.xiaominglui.app.vello.base.C;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Re-package the Log util, now we can control all the logs' switch in this
 * file(a overall switch)
 */
public class LogWritter {



    public static final SimpleDateFormat simpleDateFormatInSS = new
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    public static final SimpleDateFormat simpleDateFormat = new
            SimpleDateFormat("yyyy_MM_dd");

    public static final boolean OUTPUT_MEMORY_INFO = false;

    private static final ActivityManager am = (ActivityManager) C.get().getSystemService(Context.ACTIVITY_SERVICE);

    private static int pid;

    static {
        if (OUTPUT_MEMORY_INFO) {
            List<ActivityManager.RunningAppProcessInfo> appProcessList = am
                    .getRunningAppProcesses();

            for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessList) {
                if ("com.market2345".equals(appProcessInfo.processName)) {
                    pid = appProcessInfo.pid;
                    Log.i("LogUtil", "pid=" + pid);
                    break;
                }
            }
        }
    }


    /**
     * Low-level logging call.
     *
     * @param priority The priority/type of this log message
     * @param tag      Used to identify the source of a log message.  It usually identifies
     *                 the class or activity where the log call occurs.
     * @param msg      The message you would like logged.
     * @return The number of bytes written.
     */
    public static void println(int priority, String tag, String msg) {
        Log.println(priority, tag, msg);
    }


    public static String SDCARD_PATH = null;

    static {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            SDCARD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "2345手机助手" + File.separator + "log";
        } else {
            Context context = C.get();
            if (context != null) {
                SDCARD_PATH = context.getFilesDir().getAbsolutePath() + File.separator + "log";
            }
        }
    }


    public static void writeToFile(String tag, String msg) {
        writeToFile(null, tag, msg);
    }

    public static void writeToFile(String path, String tag, String msg) {

        Date date = new Date();
        date.setTime(System.currentTimeMillis());
        FileOutputStream output = null;
        if (TextUtils.isEmpty(path)) {
            if (TextUtils.isEmpty(SDCARD_PATH)) {
                return;
            }
            path = SDCARD_PATH;
        }

        try {
            File logFile = new File(path);
            if (!logFile.exists() && !logFile.mkdirs()) {
                return;
            }
            Time time = new Time();
            time.setToNow();
            String dateStr =time .format("%Y_%m_%d");
            String fileName = "xlog_" + dateStr + ".txt";
            File file = new File(logFile, fileName);
            output = new FileOutputStream(file, true);
            String xtime = simpleDateFormatInSS.format(date);
            String msgdata = xtime + "  " + tag + ":    " + msg + "\r\n";
            output.write(msgdata.getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
            } catch (IOException ee) {
                ee.printStackTrace();
            }
        }

    }
}