package ru.dean.dtclient.dtstructs;

/**
 * Created by explorer on 10.10.2014.
 */
public class Consts {
    public static final int SERVERPORT = 5050;
    public static final String SERVER_IP = "77.247.188.78";


    //константы с типами сообщений EmitSignal
    public static final String CHANGECLSTATE = "CHANGECLSTATE";
    public static final String USERCONNECTED = "USERCONNECTED";
    public static final String USERDISCONNECTED = "USERDISCONNECTED";
    public static final String ADDTEXTMESSAGE = "ADDTEXTMESSAGE";
    public static final String INCOMINGTEXTMESSAGE = "INCOMINGTEXTMESSAGE";




    public static final byte USERTYPE_USER = 0;
    public static final byte USERTYPE_USER_1 = 3;
    public static final byte USERTYPE_CONF = 2;
}
