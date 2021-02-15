package ru.dean.dtclient;

import android.os.Handler;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ru.dean.dtclient.dtstructs.Consts;
import ru.dean.dtclient.dtstructs.DTHeader;
import ru.dean.dtclient.dtstructs.DTTextMessage;
import ru.dean.dtclient.dtstructs.DTUser;
import ru.dean.dtclient.threads.DTSocketThread;
import ru.dean.dtclient.threads.HistoryThread;
import ru.dean.dtclient.threads.LoadCLThread;
import ru.dean.dtclient.threads.OfflineMessagesThread;
import ru.dean.dtclient.threads.OnlineUsersThread;
import ru.dean.dtclient.utils.NotificationUtils;
import ru.dean.dtclient.utils.Utils;


public class DTConnection   {
    private static DTConnection INSTANCE;
    public interface DTConnectionListener {
        public void emitSignal(String message, Object obj);
    }

    List<DTConnectionListener> listeners = new ArrayList<DTConnectionListener>();
    public Handler h;
    String login, pass;
    public int connectionState = -1;
    public String countReconnect = "";
    boolean  onsended = false; //признак того что исходящее сообщение отправляется на сервер
    int sendCount = 0; //количество попыток отправки сообщения

    public DTSocketThread dt = null;




    public void addListener(DTConnectionListener toAdd) {
        listeners.add(toAdd);
    }
    public void removeListener(DTConnectionListener toRemove) {
        for (int i = 0; i < listeners.size(); ++i) {
            if (listeners.get(i).equals(toRemove))
                listeners.remove(i);
        }

    }

    class outMessagesTimerTask extends TimerTask {

        @Override
        public void run() {
            //берем из очереди и отправляем сообщение
            sendMessageFromQueue();

        }
    };
    class pingTimerTask extends TimerTask {

        @Override
        public void run() {
            //шлем пигующее сообщения для поддержания связи с сервером
                try {
                    if (dt.client != null)
                        DTSendMessage((byte) 22, 0, "", 0, (byte) 0, 0, 0);
                } catch (UnsupportedEncodingException e) {
                    System.out.println("caught" + e);
                }
        }
    };



    public static synchronized DTConnection Inst() {
        if(INSTANCE == null) {
            Utils.appendLog("NEWINSTANCEDTConnection");
            INSTANCE = new DTConnection();
        }
        return INSTANCE;
    }
    public DTConnection() {
        System.out.println("DTConnection started");

        Timer pingTimer = new Timer();
        pingTimer.schedule(new pingTimerTask(), 30000, 50000);

        Timer outMessagesTimer = new Timer();
        outMessagesTimer.schedule(new outMessagesTimerTask(), 2000, 1000);

        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case 0:
                        byte[] buf = (byte[]) msg.obj;
                        try {
                            parseIncoming(buf);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 11:
                        for (DTConnectionListener hl : listeners)
                            hl.emitSignal("01", "");
                        break;
                    case 12:
                        afterConnection(12);

                        break;
                    case 14:
                        countReconnect = (String) msg.obj;
                        setConnectionState(0);

                        break;
                    case 20:  //offlineMessagesComplete

                        ArrayList<DTTextMessage> messageList1 = (ArrayList<DTTextMessage>)msg.obj;
                        if (messageList1 != null) {
                            if (messageList1.size() > 0){
                                Storage.Inst().messageList.addAll(messageList1);
                                for (DTConnectionListener hl : listeners)
                                    hl.emitSignal(Consts.ADDTEXTMESSAGE, new Object());

                                for (DTTextMessage m : messageList1)
                                    if (Storage.Inst().currentChatUserUID != m.sender || Utils.isApplicationBroughtToBackground(MyApplication.getInstance())) {
                                        NotificationUtils.Inst(MyApplication.getInstance()).createInfoNotification(m);
                                    }
                                   // for (DTConnectionListener hl : listeners)
                                  //      hl.emitSignal(Consts.INCOMINGTEXTMESSAGE, m);

                            }
                        }


                        break;
                    case 21:  //loadCLComplete

                        HashMap<Long, DTUser> contactList1 = (HashMap<Long, DTUser>) msg.obj;

                        //сортируем полученный список контактов
                        HashMap<Long, DTUser> contactList2 = Utils.sortByValues(contactList1);

                        Storage.Inst().acquireclSemaphore();
                        Storage.Inst().contactList.putAll(contactList2);
                        Storage.Inst().clSemaphore.release();

                        OfflineMessagesThread off = new OfflineMessagesThread(h);
                        for (DTConnectionListener hl : listeners)
                            hl.emitSignal(Consts.CHANGECLSTATE, "");
                        break;
                    case 22:  //OnlineUsersComplete
                        OfflineMessagesThread off1 = new OfflineMessagesThread(h);
                        for (DTConnectionListener hl : listeners)
                            hl.emitSignal(Consts.CHANGECLSTATE, "");
                        break;


                    case 1: //окончилась загрузка истории
                        ArrayList<DTTextMessage> histmessages = (ArrayList<DTTextMessage>) msg.obj;
                        if (histmessages != null) {
                            if (histmessages.size() > 0) {
                                Storage.Inst().messageList.addAll(histmessages);

                            }
                        }
                        break;
                }
            };
        };


        dt = new DTSocketThread(h);

    }

    public void DTSendMessage(byte AID, long AReceiver, String ABody, int ALength,
                              byte AFlag, int ATale, int ATick) throws UnsupportedEncodingException {
        byte[]  ABuff = new byte[1024];
        int iRet, i;

        DTHeader AHeader = new DTHeader();

        long l =Storage.Inst().user.UID;

        AHeader.Command  = AID;
        AHeader.Flags    = AFlag;
        AHeader.Sender   = Storage.Inst().user.UID;
        AHeader.Reciever = AReceiver;
        AHeader.Autor    = Storage.Inst().user.UID;
        AHeader.Seans    = 0;
        AHeader.Tick     = ATick;
        AHeader.Tale     = ATale;


        iRet = 40;

        if (ALength>(1024-iRet))
            ALength=1024-iRet;


        //Cp1251
        byte[] bytes = ABody.getBytes("Cp1251");
        System.arraycopy(bytes, 0, ABuff, 40, ALength);
        AHeader.Length   = (short)(iRet+ALength);
        byte[]  BBuff = new byte[40];
        BBuff = AHeader.getData();
        System.arraycopy(BBuff, 0, ABuff, 0, 40);
        dt.writeData(ABuff);

    }



    public void login(String _login, String _pass)   {
        login = _login; pass = _pass;

        dt.connect();
    }

    public void afterConnection(int error)   {


        if (error == 12) {

            String str;
            StringBuffer sb = new StringBuffer();
            str = sb.append(login).append('\r').append(pass).append("\r").toString();

            try {
                DTSendMessage((byte) 1, 0, str, str.length(), (byte) 0, 0, 0);
            } catch (UnsupportedEncodingException e) {
                System.out.println("caught" + e);
            }
        }


    }



    public void parseIncoming(byte[] buf) throws UnsupportedEncodingException {

        DTHeader DHeader = new DTHeader();
        DHeader.setData(buf);

        Utils.appendLog("DTConnection.parseIncoming " + String.valueOf(DHeader.Command) + "  " + String.valueOf(DHeader.Flags));

        //ответ на авторизацию
        if (DHeader.Command == 1)
        {
            parseAuth(DHeader, buf);

        }
        //пришло подтверждение отправки сообщения
        else if (DHeader.Command == 37)
        {
            parseMessageConfim(DHeader, buf);

        }
        //пришло сообщение от пользователя
        else if (DHeader.Command == 4)
        {
            parseMessage(DHeader, buf);

        }
        //подключение отключение пользователя
        else if (DHeader.Command == 5 || DHeader.Command == 6)
        {
            parseUserChangeState(DHeader, buf);

        }
        //изменение статуса пользователя
        else if (DHeader.Command == 34)
        {
            parseUserStat(DHeader, buf);

        }
        //ответ на пинг
        else if (DHeader.Command == 22)
        {


        }
        //DTQ_CONFLST
        else if (DHeader.Command == 12)
        {


        }
        //DTQ_CONFADD
        else if (DHeader.Command == 13)
        {
            parseConfAdd(DHeader, buf);
        }
        //DTQ_CONFDEL
        else if (DHeader.Command == 14)
        {
            parseConfDel(DHeader, buf);
        }
        else
        {
            byte cmd;
            cmd = DHeader.Command;
            //Log.d("11111111111111111111", (String)cmd);
        }

    }

    private void parseAuth(DTHeader DHeader, byte[] buf) {
        //авторизация успешна
        if (DHeader.Flags == 0)
        {

            String st = null;
            try {
                st = new String(buf, 40, (1024 - 40), "Cp1251");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String[] stt = Utils.ReadBufferStr(st);



            //если логинимя в первый раз загружаем список контактов
            if (Storage.Inst().user.UID == 0) {
                DTUser u = new DTUser();
                u.UID = DHeader.Sender;
                u.Name = stt[0];
                u.Family = stt[1];
                u.City = stt[2];
                u.Patronymic = stt[5];
                u.Occupation = stt[7];
                u.Contacts = stt[8];

                u.Login = login;
                u.Password = pass;

                Storage.Inst().user = u;
                LoadCLThread off = new LoadCLThread(h);
            }
            //иначе загружаем список онлайн и недоставленные сообщения
            else {
                Storage.Inst().acquireclSemaphore();
                //сбрасываем статусы всех юзеров на неподключено
                for (HashMap.Entry<Long, DTUser> user : Storage.Inst().contactList.entrySet())
                {
                    DTUser us = user.getValue();
                    us.Active = false;
                    Storage.Inst().contactList.put(us.UID, us);
                }
                Storage.Inst().clSemaphore.release();
                OnlineUsersThread off = new OnlineUsersThread(h);
            }


            setConnectionState(1);



        }
        //Неизвестный абонент
        else if (DHeader.Flags == 1)
        {
            for (DTConnectionListener hl : listeners)
                hl.emitSignal("02", "");

            dt.disconnect();

        }
        //Неверный пароль
        if (DHeader.Flags == 2)
        {
            for (DTConnectionListener hl : listeners)
                hl.emitSignal("03", "");

            dt.disconnect();

        }
        //Абонент уже подключен
        if (DHeader.Flags == 3)
        {
            for (DTConnectionListener hl : listeners)
                hl.emitSignal("04", "");

            dt.disconnect();

        }
    }

    static int id = 0;
    public void sendMessage(String text, long UID)
    {

        DTTextMessage m = new DTTextMessage();
        m.ID = ++id;
        m.author = Storage.Inst().user.UID;
        m.sender = Storage.Inst().user.UID;
        m.receiver = UID;
        m.Body = text;
        m.datetime = new Date();

        //сообщение пользователю
        if (Utils.isLikeUser(Storage.Inst().contactList.get(UID))) {
            m.command = 4;
        } else { //сообщение в конференцию
            m.command = 16;
        }

        Storage.Inst().outcomingMessageList.add(m);
        Storage.Inst().messageList.add(m);

        sendMessageFromQueue();

        for (DTConnectionListener hl : listeners)
            hl.emitSignal(Consts.ADDTEXTMESSAGE, m);

    }


    public void sendMessageFromQueue() {

        //инкрементируем счетчик отправки сообщения если оно отправляеться
        if (onsended == true) {
            sendCount++;
        }
        //если счетчик отправки сообщений > 10 это значит что подтверждение получения сообщения от сервера
        //не пришло. Поэтому сбрасываем все счетчики, чтобы сообщение отправилось еще раз
        if (sendCount > 10) {
            onsended = false;
            sendCount = 0;
        }



        if (Storage.Inst().outcomingMessageList.size() != 0 && onsended == false && connectionState == 1) {
            onsended = true;
            DTTextMessage m = Storage.Inst().outcomingMessageList.peek();
            try {
                DTSendMessage(m.command, m.receiver, m.Body, m.Body.length(), (byte) 0, 0, 1);
            } catch (UnsupportedEncodingException e) {
                System.out.println("caught" + e);
            }
        }
    }


    private void parseMessage(DTHeader DHeader, byte[] buf) {

        long ARes = buf[0];
        ARes =(ARes<<8)+buf[1];

        //добавляем пришедшее соощение к списку сообщений пользователя
        DTTextMessage m = new DTTextMessage();
        String st = null;
        try {
            st = new String(buf, 40, ((int)ARes - 40), "Cp1251");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        m.author = DHeader.Autor;
        m.sender = DHeader.Sender;
        m.state = 1;
        m.Body = st;
        m.datetime = new Date();

        //если в контакт листе есть пользователь с UID от которого пришло сообщение
        if (Storage.Inst().contactList.containsKey(m.sender)) {

            //если не было загружены ни одного сообщения для текущего чата загружаем историю и не добавляем это сообщение
            if (Storage.Inst().contactList.get(m.sender).FirstMessageID == 0) {
                HistoryThread hist = new HistoryThread(h, m.sender, Storage.Inst().contactList.get(m.sender).FirstMessageID, 3);
                Storage.Inst().contactList.get(m.sender).FirstMessageID = 1;
            } else {
                Storage.Inst().messageList.add(m);
                for (DTConnectionListener hl : listeners)
                    hl.emitSignal(Consts.ADDTEXTMESSAGE, new Object());
            }


            //устанавливаем приоритет юзера в списке последних диалогов
            Storage.Inst().addUserToLast(m.sender);


            if (Storage.Inst().currentChatUserUID != m.sender || Utils.isApplicationBroughtToBackground(MyApplication.getInstance())) {
                NotificationUtils.Inst(MyApplication.getInstance()).createInfoNotification(m);
            }
        }

    }


    private void parseMessageConfim(DTHeader DHeader, byte[] buf) {
        //удаляем сообщение из очереди и помещаем его в список
        onsended = false;
        sendCount = 0; //сбрасываем счетчик отправкит сообщений в 0
        if (Storage.Inst().outcomingMessageList.size() != 0) {
            DTTextMessage m = Storage.Inst().outcomingMessageList.poll();
            m.state = 1;

            //перезаписываем сообщение в списке с новым статусом
            for (int i = 0; i < Storage.Inst().messageList.size(); i++)
                if (Storage.Inst().messageList.get(i).ID == m.ID)
                    Storage.Inst().messageList.set(i, m);

            for (DTConnectionListener hl : listeners)
                hl.emitSignal(Consts.ADDTEXTMESSAGE, new Object());
        }
    }

    private void parseUserChangeState(DTHeader DHeader, byte[] buf) {
        Storage.Inst().acquireclSemaphore();
        DTUser u = Storage.Inst().contactList.get(DHeader.Sender);
        //подключен
        if (DHeader.Command == 5) {
            u.StatusID = DHeader.Flags;
            u.Active = true;
        }
        //отключен
        if (DHeader.Command == 6) {
            u.StatusID = 0;
            u.Active = false;
        }
        Storage.Inst().contactList.put(u.UID, u);

        for (DTConnectionListener hl : listeners)
            hl.emitSignal(Consts.CHANGECLSTATE, "");

        Storage.Inst().clSemaphore.release();
    }

    private void parseUserStat(DTHeader DHeader, byte[] buf) {
        Storage.Inst().acquireclSemaphore();

        DTUser u = Storage.Inst().contactList.get(DHeader.Sender);
        if (u.Active)
            u.StatusID = DHeader.Flags;

        Storage.Inst().contactList.put(u.UID, u);

        for (DTConnectionListener hl : listeners)
            hl.emitSignal(Consts.CHANGECLSTATE, "");

        Storage.Inst().clSemaphore.release();
    }

    private  void parseConfAdd(DTHeader DHeader, byte[] buf) {
        long UID = Utils.ReadIBO(buf);
        String st = null;
        try {
            st = new String(buf, 20, (1024 - 20), "Cp1251");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String[] stt = Utils.ReadBufferStr(st);

        DTUser u = new DTUser();
        u.UID = UID;
        u.Active = true;
        u.UserType = Consts.USERTYPE_CONF;
        u.NickName = stt[0];
        u.Login = stt[0];
        u.Priority = 0;

        Storage.Inst().contactList.put(u.UID, u);
        for (DTConnectionListener hl : listeners)
            hl.emitSignal(Consts.CHANGECLSTATE, "");


    }

    private  void parseConfDel(DTHeader DHeader, byte[] buf) {
        long UID = Utils.ReadIBO(buf);
        Storage.Inst().contactList.remove(UID);
        for (DTConnectionListener hl : listeners)
            hl.emitSignal(Consts.CHANGECLSTATE, "");

    }



    private void setConnectionState (int state) {
        connectionState = state;

        if (connectionState == 0)
        {
            for (DTConnectionListener hl : listeners)
                hl.emitSignal(Consts.USERDISCONNECTED, "");
        }
        if (connectionState == 1)
        {
            for (DTConnectionListener hl : listeners)
                hl.emitSignal(Consts.USERCONNECTED, this);
        }
    }


}


//
//
//    procedure PerfMessage(pBuff : array of byte; pLen : integer);
//begin
//        case pBuff[2] of
                           //        DTQ_RECONNECT   : pmReconnect(pBuff, pLen);     //Передоключение...
                                //        DTQ_ABLIST      : pmAbonentList(pBuff, pLen);   //Список абонентов юзера
                                //        DTQ_TEXT        : pmGetMessage(pBuff, pLen);    //Пришло сообщение
                             //        DTQ_USERON      : pmUserOn(pBuff, pLen);        //Подключение пользователя
                                  //        DTQ_USEROFF     : pmUserOff(pBuff, pLen);       //Отключение пользователя
//        DTQ_CONFLST     : pmConfList(pBuff, pLen);      //Список участников конференции
//        DTQ_CONFADD     : pmConfAdd(pBuff, pLen);       //Добавление конференции
//        DTQ_CONFDEL     : pmConfDel(pBuff, pLen);       //Удаление конференции
//        DTQ_GETFILE     : pmFileGet(pBuff, pLen);       //Получение файла
//        DTQ_CIRCNUM     : pmGetCircNum(pBuff, pLen);    //Получение номера циркуляра
//        DTQ_GETCIRC     : pmGetCircular(pBuff, pLen);   //Получение циркуляра
//        DTQ_CIRCLST     : pmCircularList(pBuff, pLen);  //Получение истории циркуляров
                                      //        DTQ_PING        : pmPingMessage(pBuff, pLen);   //Пинг
                                  //        DTQ_USERSTAT    : pmUserStatusChange(pBuff, pLen);//Изменение статуса пользователя
//        DTQ_TEXTERR     : pmGetErrorMessage(pBuff, pLen);//Ошибка при доставке сообщения
//        DTQ_SETUI       : pmUserInfoChange(pBuff, pLen);//Изменение личной информации пользователя
                                 //        DTQ_TEXTSUCC    : pmUserMessageDeliver(pBuff, pLen);//Подтверждение отправки сообщения
//        DTQ_CALL        : pmVoIPCall(pBuff, pLen);       //Звонок
//        DTQ_CALLRE      : pmVoIPState(pBuff, pLen);      //Состояние звонка
//        DTQ_CORRECTION  : pmCorrection(pBuff, pLen);     //Коррекция
//        DTQ_SUPPLYNOTIFY: pmSupplyNotify(pBuff, pLen);   //Уведомление о заявках
//        DTQ_MOVE        : pmStoreMove(pBuff, pLen);      //Уведомление о смене бирки
//        end;
//        end;
//
//        end.
//
//
//
//
//        const
//        { Запросы (Dean Talk Query) }
//        DTQ_RECONNECT = 1;
//        DTQ_ABLIST    = 2;
//        DTQ_TEXT      = 4;
//        DTQ_USERON    = 5;
//        DTQ_USEROFF   = 6;
//        DTQ_CONFLST   = 12;
//        DTQ_CONFADD   = 13;
//        DTQ_CONFDEL   = 14;
//        DTQ_GETFILE   = 17;
//        DTQ_CIRCNUM   = 18;
//        DTQ_GETCIRC   = 19;
//        DTQ_CIRCLST   = 21;
//        DTQ_PING      = 22;
//        DTQ_USERSTAT  = 34;
//        DTQ_TEXTERR   = 35;
//        DTQ_SETUI     = 36;
//        DTQ_TEXTSUCC  = 37;
//
//        { Флаги подключения (DeanTalkConnectFlags) }
//        DTCF_SUCESS = 0;
//        DTCF_USER   = 1;
//        DTCF_PSWD   = 2;
//        DTCF_ONLINE = 3;