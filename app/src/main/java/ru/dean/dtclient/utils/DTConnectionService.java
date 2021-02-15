package ru.dean.dtclient.utils;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class DTConnectionService extends Service {
    private static DTConnectionService INSTANCE;
    Alarm alarm = new Alarm();
    String s = new String("");

    MyBinder binder = new MyBinder();

    public DTConnectionService() {

    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        startService(rootIntent);
    }

    public void onCreate() {

        super.onCreate();
        Log.d("", "onCreate");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("", "onStartCommand");
        //alarm.SetAlarm(DTConnectionService.this);
        return super.onStartCommand(intent, flags, startId);
    }



    public void onDestroy() {
        alarm.CancelAlarm(getApplicationContext());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onDestroy();
        Log.d("", "onDestroy");
    }

//    public void onStart(Context context,Intent intent, int startId)
//    {
//        alarm.SetAlarm(DTConnectionService.this);
//    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.d("", "MyService onBind");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("", "MyService onBind");
        //return super.onUnbind(intent);
        return false;
    }

    class MyBinder extends Binder {
        DTConnectionService getService() {
            return DTConnectionService.this;
        }
    }
}
