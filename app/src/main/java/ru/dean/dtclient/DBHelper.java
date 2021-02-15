package ru.dean.dtclient;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private SQLiteDatabase db;

    public DBHelper(Context context) {
        super(context, "dtclientdb", null, 27);    //последний аргумент версия бд

        db = getWritableDatabase();

        Storage.Inst().priorityMax = selectMaxPriority();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // создаем таблицу с полями
        db.execSQL("create table last ("
                + "id integer primary key autoincrement,"
                + "userid integer,"
                + "priority integer" + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if(newVersion > oldVersion){
            db.execSQL("DROP TABLE IF EXISTS last");
            onCreate(db);
        }

    }


    //максимальный приоритет юзера
    private int selectMaxPriority() {
        int maxPriority = 0;
        Cursor c = db.rawQuery("SELECT MAX(priority) FROM last", null);
        if (c.moveToFirst()) {
            maxPriority = c.getInt(0);
        }

        return maxPriority;
    }
    //добавить юзера с последним
    public void addLastUser(Long id, Integer priority) {

        Integer version = db.getVersion();

        ContentValues cv = new ContentValues();
        cv.put("userid", id);
        cv.put("priority", priority);
        //смотрим есть ли запись о юзере с данным id в таблице
        Cursor c = db.rawQuery("SELECT * FROM last WHERE userid = ?", new String[] {String.valueOf(id)});

        //если есть обновляем
        if (c.moveToFirst()) {
            int updCount = db.update("last", cv, "userid = ?", new String[] { String.valueOf(id) });

        } else {  //если нет добавляем
            // вставляем запись и получаем ее ID
            long rowID = db.insert("last", null, cv);
        }
    }
    //запросить приоритет юзера
    public int getUserPriority(Long id) {
        int priority = 0;
        Cursor c = db.rawQuery("SELECT * FROM last WHERE userid = ?", new String[]{String.valueOf(id)});
        if (c.moveToFirst()) {
            priority = c.getInt(2);
        }
        return priority;
    }


}
