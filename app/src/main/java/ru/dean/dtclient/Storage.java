package ru.dean.dtclient;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import ru.dean.dtclient.dtstructs.DTTextMessage;
import ru.dean.dtclient.dtstructs.DTUser;
import ru.dean.dtclient.utils.Utils;

public class Storage {
    private static Storage INSTANCE;

    public DBHelper dbHelper;

    public DTUser user = new DTUser();

    public LinkedHashMap<Long, DTUser> contactList = new LinkedHashMap<Long, DTUser>();
    public int priorityMax = 0;
    public Semaphore clSemaphore = new Semaphore(1);


    public Queue<DTTextMessage> outcomingMessageList = new LinkedList<DTTextMessage>();
    public ArrayList<DTTextMessage> messageList = new ArrayList<DTTextMessage>();

    public long currentChatUserUID = 0;
    //путь сохраненного с камеры файла для отправки
    public File file_path;



    public static synchronized Storage Inst() {
        if(INSTANCE == null) {
            Utils.appendLog("NEWINSTANCEStorage");
            INSTANCE = new Storage();
        }
        return INSTANCE;
    }
    private Storage() {

    }


    public void acquireclSemaphore() {
        try {
            clSemaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    //устанавливаем приоритет юзера в списке последних диалогов
    public void addUserToLast(Long UID) {
        acquireclSemaphore();
        DTUser user = contactList.get(UID);
        user.Priority = ++priorityMax;
        Storage.Inst().contactList.put(user.UID, user);
        clSemaphore.release();

        dbHelper.addLastUser(UID, user.Priority);

    }





}
