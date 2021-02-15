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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Date;

import ru.dean.dtclient.Storage;
import ru.dean.dtclient.dtstructs.Consts;
import ru.dean.dtclient.dtstructs.DTHeader;
import ru.dean.dtclient.dtstructs.DTTextMessage;
import ru.dean.dtclient.dtstructs.DTUser;
import ru.dean.dtclient.utils.Utils;

public class HistoryThread implements Runnable {
    public Socket client;
    public Thread thread;
    private Handler h;
    private Long UID;
    private int Seans;
    private int Count;

    DataOutputStream dataOutputStream= null;
    DataInputStream dataInputStream = null;
    ArrayList<DTTextMessage> messageList = new ArrayList<DTTextMessage>();

    public HistoryThread(Handler _h, Long _UID, int _Seans, int _Count) {
        h = _h;
        UID = _UID;
        Seans = _Seans;
        Count = _Count;
        thread = new Thread(this, "HistoryThread");
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
                loadHistory();

                byte[] buf = new byte[1024];
                int r;
                r = dataInputStream.read(buf);
                if (r > 0) {

                    if (buf[0] == 0)
                        break;
                    parseHistory(buf);
                } else {
                    break;
                }
            }


            Message m = h.obtainMessage(1, messageList);
            h.sendMessage(m);

            client.close();

        } catch (Exception e) {
            e.printStackTrace();
        }


        Log.d("vu", "Thread Stop");

    }

    public void loadHistory() {

        byte[]  ABuff = new byte[1024];
        DTHeader AHeader = new DTHeader();

        AHeader.Command  = 17;
        AHeader.Flags    = 62;
        AHeader.Sender   = Storage.Inst().user.UID;
        AHeader.Reciever = UID;
        AHeader.Autor    = 0;
        AHeader.Seans    = Seans;
        AHeader.Tick     = 0;
        AHeader.Tale     = Count;

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


    private void parseHistory(byte[] buf) {

        DTTextMessage m = new DTTextMessage();



        ByteBuffer bb = ByteBuffer.allocateDirect(25);
        bb.order( ByteOrder.LITTLE_ENDIAN);
        bb.put(buf, 0, 25);


        bb.get(0);
        int messageID = bb.getInt(1);
        int AuthorID = bb.getInt(5);
        Double dateTime = bb.getDouble(9);
        int messageType = bb.getInt(17);
        int orderNum = bb.getInt(21);

        //присваеиваем айдишник последнего сообщения юзеру
        Storage.Inst().contactList.get(UID).FirstMessageID = messageID;

        String st = null;

        int i = 0;
        for (i = 25; i < 1024; i++) {
            if (buf[i] == 0)
                break;
        }

        try {
            st = new String(buf, 25, (i - 25), "Cp1251");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Double unixdate = (dateTime - 25569.0) * 86400000.0;


        //если загружаем историю сообщений с пользователем типа пользователь
        if (Utils.isLikeUser(Storage.Inst().contactList.get(UID))) {
            m.author = Long.valueOf(AuthorID);
            m.sender = Long.valueOf(AuthorID);
        } else {   //если загружаем историю сообщений с пользователем типа конференция
            //если отправитель сообщения я то присваиваем полю сендер свой айдишник
            m.author = Long.valueOf(AuthorID);
            if (Long.valueOf(AuthorID).equals(Storage.Inst().user.UID)) {
                m.sender = Long.valueOf(AuthorID);
            } else { //если отправитель не я згачит присваиваем полю сендер айдишник конференции
                m.sender = UID;
            }
        }

        if (m.sender.equals(UID)) //если отправитель сообщения не я то получаткль я
            m.receiver = Storage.Inst().user.UID;
        else
            m.receiver = UID;

        m.state = 1;
        m.Body = st;
        m.datetime = new Date(unixdate.longValue());
        m.datetime.setTime(m.datetime.getTime() - (4*60*60*1000));

        messageList.add(m);

    }
}
