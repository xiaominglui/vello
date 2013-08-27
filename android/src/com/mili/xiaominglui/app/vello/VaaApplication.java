package com.mili.xiaominglui.app.vello;


import android.app.Application;

import com.avos.avoscloud.Parse;
import com.avos.avoscloud.ParseACL;
import com.avos.avoscloud.ParseUser;
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
        
        Parse.useAVCloudCN();
        Parse.initialize(this, "ycvs6d1qc9jdi752mayrvte0dq6nhs1e2kub1hrf3pkmhds2", "rixsj1ev775x0e3sd6h7s1o6cydgcgets0q6keb4ihk9t2x2");

    }
}
