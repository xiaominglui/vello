package com.mili.xiaominglui.app.vello;


import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;
import android.media.AsyncPlayer;

import com.avos.avoscloud.AVOSCloud;
import com.mili.xiaominglui.app.vello.base.C;
import com.mili.xiaominglui.app.vello.util.ACRATrelloSender;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

@ReportsCrashes(
        formKey = "",
        customReportContent = {ReportField.LOGCAT},
        logcatArguments = { "-t", "100", "-v", "long", "*:E" })
public class VaaApplication extends Application {
    private static final String TAG = VaaApplication.class.getSimpleName();
    private static AsyncPlayer mAsyncPlayer;
    @Override
    public void onCreate() {
        ACRA.init(this);
        ACRATrelloSender trelloSender = new ACRATrelloSender(getApplicationContext());
        ACRA.getErrorReporter().removeAllReportSenders();
        ACRA.getErrorReporter().setReportSender(trelloSender);
        
        AVOSCloud.useAVCloudCN();
        AVOSCloud.initialize(this, "ycvs6d1qc9jdi752mayrvte0dq6nhs1e2kub1hrf3pkmhds2", "rixsj1ev775x0e3sd6h7s1o6cydgcgets0q6keb4ihk9t2x2");

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);

        if (mAsyncPlayer == null) {
            mAsyncPlayer = new AsyncPlayer(TAG);
        }

        super.onCreate();
        C.setContext(getApplicationContext());
    }

    public static AsyncPlayer getAsyncPlayer() {
        return mAsyncPlayer;
    }

    @Override
    public void onTerminate() {
        mAsyncPlayer.stop();
        mAsyncPlayer = null;
        super.onTerminate();
    }
}
