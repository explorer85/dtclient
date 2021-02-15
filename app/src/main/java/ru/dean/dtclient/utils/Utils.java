package ru.dean.dtclient.utils;


import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ru.dean.dtclient.dtstructs.Consts;
import ru.dean.dtclient.dtstructs.DTUser;

public class Utils {

    public static String[] ReadBufferStr(String st)
    {
        String[] stt = st.split("\r");
        String s = stt[stt.length-1].split("‡")[0];
        stt[stt.length-1] = s;

        return stt;
    }


    public static long ReadIBO(byte[] buf)
    {
        long ARes = 0;
        for (int i = 0; i < 8; i++) {
            ARes =(ARes<<8)+(buf[4+i] & 0xFF);
        }
        return ARes;
    }


    public static boolean isApplicationBroughtToBackground(Context context)
    {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningTaskInfo> tasks = new ArrayList<ActivityManager.RunningTaskInfo>();
        tasks.addAll(am.getRunningTasks(1));
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }

        return false;
    }

    public static void appendLog(String text)
    {
       String tt = (new SimpleDateFormat("[HH:mm:ss]")).format(new Date()) + " -- " + text;

        File logFile = new File("sdcard/dtclientlog.file");
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(tt);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }



    public static HashMap sortByValues(HashMap map) {
        ArrayList<DTUser> list = new ArrayList<DTUser>(map.values());

        // сортируем мап своим компаратором
        Collections.sort(list, new Comparator<DTUser>() {
            public int compare(DTUser o1, DTUser o2) {
                return  o1.Family.compareTo(o2.Family);
            }
        });

        // Here I am copying the sorted list in HashMap
        // using LinkedHashMap to preserve the insertion order
        HashMap sortedHashMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            DTUser u = (DTUser)it.next();
            sortedHashMap.put(u.UID, u);
        }
        return sortedHashMap;
    }

    //возвращает true если тип пользователя пользователь false если тип пользователя конференция
    public static boolean isLikeUser(DTUser u)
    {
        if (u.UserType == Consts.USERTYPE_USER || u.UserType == Consts.USERTYPE_USER_1) {
            return true;
        } else if (u.UserType == Consts.USERTYPE_CONF || u.UserType == 1) {
            return false;
        }
        return true;
    }

}
