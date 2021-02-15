package ru.dean.dtclient.threads;


import android.os.Handler;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;

import ru.dean.dtclient.Storage;
import ru.dean.dtclient.dtstructs.Consts;
import ru.dean.dtclient.dtstructs.DTHeader;
import ru.dean.dtclient.dtstructs.DTTextMessage;
import ru.dean.dtclient.dtstructs.DTUser;

public class OnlineUsersThread implements Runnable  {
    public Socket client;
    public Thread thread;
    private Handler h;

    DataOutputStream dataOutputStream= null;
    DataInputStream dataInputStream = null;

    public OnlineUsersThread(Handler _h) {
        h = _h;
        thread = new Thread(this, "OnlineUsersThread");
        thread.start(); // Запускаем поток
    }

    @Override
    public void run() {

        InetAddress serverAddr = null;
        try {
            serverAddr = InetAddress.getByName(Consts.SERVER_IP);

            client = new Socket(serverAddr, Consts.SERVERPORT);
            dataOutputStream = new DataOutputStream(client.getOutputStream());
            dataInputStream = new DataInputStream(client.getInputStream());


            while (true) {
                loadOnlineUsers();

                byte[] buf = new byte[1024];
                int r;
                r = dataInputStream.read(buf);
                if (r > 0) {
                    DTHeader DHeader = new DTHeader();
                    DHeader.setData(buf);

                    if (DHeader.Reciever == -1)
                        break;

                    parseOnlineUsers(DHeader, buf);
                } else {
                    break;
                }
            }


            h.sendEmptyMessage(22);
            client.close();

        } catch (Exception e) {
            e.printStackTrace();
        }


        Log.d("vu", "Thread Stop");

    }

    public void loadOnlineUsers() {

        byte[]  ABuff = new byte[1024];
        DTHeader AHeader = new DTHeader();

        AHeader.Command  = 17;
        AHeader.Flags    = 18;
        AHeader.Sender   = Storage.Inst().user.UID;
        AHeader.Reciever = -1;
        AHeader.Autor    = Storage.Inst().user.UID;
        AHeader.Seans    = 0;
        AHeader.Tick     = 0;
        AHeader.Tale     = 0;

        AHeader.Length   = (short)(40+8);


        byte[]  BBuff = new byte[40];
        BBuff = AHeader.getData();
        System.arraycopy(BBuff, 0, ABuff, 0, 40);

        try {
            dataOutputStream.write(ABuff);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void parseOnlineUsers(DTHeader DHeader, byte[] buf) {

        Storage.Inst().acquireclSemaphore();
        DTUser u = Storage.Inst().contactList.get(DHeader.Reciever);
        u.Active = true;
        u.StatusID = DHeader.Flags;
        Storage.Inst().contactList.put(u.UID, u);
        Storage.Inst().clSemaphore.release();

    }
}
