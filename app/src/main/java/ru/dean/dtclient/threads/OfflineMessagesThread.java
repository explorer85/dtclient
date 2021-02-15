package ru.dean.dtclient.threads;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;

import ru.dean.dtclient.Storage;
import ru.dean.dtclient.dtstructs.Consts;
import ru.dean.dtclient.dtstructs.DTHeader;
import ru.dean.dtclient.dtstructs.DTTextMessage;
import ru.dean.dtclient.dtstructs.DTUser;


public class OfflineMessagesThread implements Runnable {

    public Socket client;
    public Thread thread;
    private Handler h;

    DataOutputStream dataOutputStream= null;
    DataInputStream dataInputStream = null;
    ArrayList<DTTextMessage> messageList = new ArrayList<DTTextMessage>();

    public OfflineMessagesThread(Handler _h) {
        h = _h;
        thread = new Thread(this, "OfflineMessagesThread");
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
                loadOfflineMessages();

                byte[] buf = new byte[1024];
                int r;
                r = dataInputStream.read(buf);
                if (r > 0) {
                    DTHeader DHeader = new DTHeader();
                    DHeader.setData(buf);

                    if (DHeader.Tale == -1)
                        break;

                    parseOfflineMessage(DHeader, buf);
                } else {
                    break;}
            }

            Message m = h.obtainMessage(20, messageList);
            h.sendMessage(m);
            client.close();

        } catch (Exception e) {
            e.printStackTrace();
        }



        Log.d("vu", "Thread Stop");

    }

    public void loadOfflineMessages() {

        byte[]  ABuff = new byte[1024];
        DTHeader AHeader = new DTHeader();

        AHeader.Command  = 17;
        AHeader.Flags    = 15;
        AHeader.Sender   = Storage.Inst().user.UID;
        AHeader.Reciever = Storage.Inst().user.UID;
        AHeader.Autor    = Storage.Inst().user.UID;
        AHeader.Seans    = 0;
        AHeader.Tick     = 0;
        AHeader.Tale     = 0;


        byte[]  BBuff = new byte[40];
        BBuff = AHeader.getData();
        System.arraycopy(BBuff, 0, ABuff, 0, 40);

        try {
            dataOutputStream.write(ABuff);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    private void parseOfflineMessage(DTHeader DHeader, byte[] buf) {
        //если в контакт листе есть пользователь с UID от которого пришло сообщение
        if (Storage.Inst().contactList.containsKey(DHeader.Sender)) {

            DTTextMessage m = new DTTextMessage();
            String st = null;
            try {
                st = new String(buf, 40, (1024 - 40), "Cp1251");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            m.author = DHeader.Autor;
            m.sender = DHeader.Sender;
            m.state = 1;
            m.Body = st;
            m.datetime = new Date();
            messageList.add(m);

            //присваеиваем айдишник последнего сообщения юзеру
            if (Storage.Inst().contactList.get(m.sender).FirstMessageID == 0) {
                Storage.Inst().contactList.get(m.sender).FirstMessageID = DHeader.Tale;
            }


        }
    }

}






