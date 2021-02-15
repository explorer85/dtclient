package ru.dean.dtclient.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.IntegerRes;

import java.util.HashMap;

import ru.dean.dtclient.MyActivity;
import ru.dean.dtclient.R;
import ru.dean.dtclient.Storage;
import ru.dean.dtclient.dtstructs.DTTextMessage;

/**
 * Created by explorer on 14.10.2014.
 */
public class NotificationUtils {

    private static final String TAG = NotificationUtils.class.getSimpleName();

    private static NotificationUtils instance;

    private static Context context;
    private NotificationManager manager; // Системная утилита, упарляющая уведомлениями
    private int lastId = 0; //постоянно увеличивающееся поле, уникальный номер каждого уведомления
    private HashMap<Integer, Notification> notifications; //массив ключ-значение на все отображаемые пользователю уведомления


    private NotificationUtils(Context context) {
        this.context = context;
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifications = new HashMap<Integer, Notification>();
    }


    public static NotificationUtils Inst(Context context) {
        if (instance == null) {
            instance = new NotificationUtils(context);
        } else {
            instance.context = context;
        }
        return instance;
    }


    public int createInfoNotification(DTTextMessage message){
        Intent notificationIntent = new Intent(context, MyActivity.class); // по клику на уведомлении откроется HomeActivity
        notificationIntent.putExtra("UID", message.sender);

        String author = "";
        if (Storage.Inst().contactList.containsKey(message.author)) {
            author = Storage.Inst().contactList.get(message.author).Family + " " + Storage.Inst().contactList.get(message.author).Name;
        }

            String text = author + ": " + message.Body;

        Notification.Builder nb = new Notification.Builder(context)
                .setSmallIcon(R.drawable.logo) //иконка уведомления
                .setAutoCancel(true) //уведомление закроется по клику0 на него
                .setTicker(text) //текст, который отобразится вверху статус-бара при создании уведомления
                .setContentText(text) // Основной текст уведомления
                .setContentIntent(PendingIntent.getActivity(context, message.sender.intValue(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT))  //message.sender.intValue() для того чтобы новый интент генерился
                .setWhen(System.currentTimeMillis()) //отображаемое время уведомления
                .setContentTitle("ДеанТалк") //заголовок уведомления
                .setDefaults(Notification.DEFAULT_ALL); // звук, вибро и диодный индикатор выставляются по умолчанию

        Notification notification = nb.getNotification(); //генерируем уведомление
        manager.notify((int) (long)message.sender, notification); // отображаем его пользователю.
        notifications.put((int) (long)message.sender, notification); //теперь мы можем обращаться к нему по id
        return lastId++;
    }

    public void deleteAllNotifications() {
        manager.cancelAll();
    }

}
