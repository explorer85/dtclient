package ru.dean.dtclient;


import org.acra.*;
import org.acra.annotation.*;
import android.app.Application;
import android.content.res.Configuration;

import ru.dean.dtclient.utils.Utils;


@ReportsCrashes(formKey = "", // will not be used
        mailTo = "explorerklg@gmail.com", // my email here
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.acra_error_header)

public class MyApplication extends Application {
    private static MyApplication singleton;

    public String global = "gool";
    // Returns the application instance
    public static MyApplication getInstance() {
        return singleton;
    }

    public final void onCreate() {
        super.onCreate();

        ACRA.init(this);
        // ErrorReporter.getInstance().checkReportsOnApplicationStart();

        singleton = this;

        Utils.appendLog("MYAPPLICATIONCREATE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!2" + global);
        global = "nooo";
        Storage.Inst();
        Storage.Inst().dbHelper = new DBHelper(getInstance());
        DTConnection.Inst();


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //Utils.appendLog("MYAPPLICATIONCONFIGCHANGRD!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Utils.appendLog("MYAPPLICATIONONTERMINATE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }
}
