package ru.dean.dtclient.dtstructs;

import java.util.Date;

/**
 * Created by explorer on 06.10.2014.
 */
public class DTTextMessage {
    public int ID;
    public Long author = new Long(0);
    public Long sender;
    public Long receiver;
    public String Body;
    public int state = 0;
    public Date datetime;
    public byte command = 0; //тип отправляемого сообщения (пользователю, конференции, всем, всем активным)

}
