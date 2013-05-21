package com.mili.xiaominglui.app.vello;


import android.app.Application;

import com.mili.xiaominglui.app.vello.util.ACRATrelloSender;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.annotation.ReportsCrashes;

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
        super.onCreate();
        
    }
}
