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
import java.util.HashMap;

import ru.dean.dtclient.Storage;
import ru.dean.dtclient.utils.Utils;
import ru.dean.dtclient.dtstructs.Consts;
import ru.dean.dtclient.dtstructs.DTHeader;
import ru.dean.dtclient.dtstructs.DTUser;

public class LoadCLThread implements Runnable {

    public Socket client;
    public Thread thread;
    private Handler h;

    DataOutputStream dataOutputStream= null;
    DataInputStream dataInputStream = null;
    HashMap<Long, DTUser> contactList = new HashMap<Long, DTUser>();

    public LoadCLThread(Handler _h) {
        h = _h;
        thread = new Thread(this, "LoadCLThread");
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


            //загрузка списка контактов
            byte flag = 5;
            load(flag);
            //загрузка списка конференций
            flag = 6;
            load(flag);

            Message m = h.obtainMessage(21, contactList);
            h.sendMessage(m);


            client.close();

        } catch (Exception e) {
            e.printStackTrace();
        }



        Log.d("vu", "Thread Stop");

    }

    //загрузить список пользователей или конференций
    public void load(byte flag) throws IOException {

        long ARes = -1;
        while (true) {
            loadCL(ARes, flag);

            byte[] buf = new byte[1024];
            int r;
            r = dataInputStream.read(buf);
            if (r > 0) {
                DTHeader DHeader = new DTHeader();
                DHeader.setData(buf);

                if (buf[3] == 1)
                    break;

                ARes = parseCL(DHeader, buf);
            } else {
                break;}


        }

    }



    public void loadCL(long UserID, byte flag)
    {
        byte[]  ABuff = new byte[1024];
        DTHeader AHeader = new DTHeader();

        long AUserID = UserID;

        AHeader.Command  = 17;
        AHeader.Flags    = flag;
        AHeader.Sender   = Storage.Inst().user.UID;
        AHeader.Reciever = Storage.Inst().user.UID;
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







    private long parseCL(DTHeader DHeader, byte[] buf) {
        long ARes = Utils.ReadIBO(buf);

            String st = null;
            try {
                st = new String(buf, 23, (1024 - 23), "Cp1251");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String[] stt = Utils.ReadBufferStr(st);

            DTUser u = new DTUser();
            u.UID = ARes;

            u.StatusID = buf[21];
            u.Active = u.StatusID > 0;
            u.UserType = buf[22];

            u.NickName = stt[0];
            u.Name = stt[1];
            u.City = stt[2];
            u.Family = stt[3];
            u.Login = stt[4];
            u.CustomGroupName = stt[6];

        //если тип пользователь то для него считываем следующие поля
        if (Utils.isLikeUser(u)) {
            u.Position = stt[7];
            u.Patronymic = stt[8];
            u.Phone = stt[9];
            u.Occupation = stt[10];
            u.Contacts = stt[11];
        }

        u.Priority =  Storage.Inst().dbHelper.getUserPriority(u.UID);

        contactList.put(u.UID, u);

        return ARes;
    }

}
