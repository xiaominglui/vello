package com.mili.xiaominglui.app.vello;


import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

import com.avos.avoscloud.AVOSCloud;
import com.mili.xiaominglui.app.vello.util.ACRATrelloSender;

@ReportsCrashes(
        formKey = "",
        customReportContent = {ReportField.LOGCAT},
        logcatArguments = { "-t", "100", "-v", "long", "*:E" })
public class VaaApplication extends Application {
    @Override
    public void onCreate() {
        ACRA.init(this);
        ACRATrelloSender trelloSender = new ACRATrelloSender(getApplicationContext());
        ACRA.getErrorReporter().removeAllReportSenders();
        ACRA.getErrorReporter().setReportSender(trelloSender);
        
        AVOSCloud.useAVCloudCN();
        AVOSCloud.initialize(this, "ycvs6d1qc9jdi752mayrvte0dq6nhs1e2kub1hrf3pkmhds2", "rixsj1ev775x0e3sd6h7s1o6cydgcgets0q6keb4ihk9t2x2");
        
        super.onCreate();
    }
}
