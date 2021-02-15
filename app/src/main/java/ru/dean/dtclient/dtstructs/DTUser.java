package ru.dean.dtclient.dtstructs;


import java.util.ArrayList;

public class DTUser {

    public Long UID = new Long(0);
    public String NickName;
    public String Login;
    public String Password;

    public String Name;
    public String Family;
    public String Patronymic;
   // BirthDay : TDateTime;
    public String City;
    public String Phone;
    public String Position;
    public String Occupation;
    public String CustomGroupName;

    public String Contacts;
    public byte StatusID;   // 0 - отключен, 1 - подключен, 2 - занят, 3 - отошел, 4 - на обеде
    public boolean Active;
    public byte UserType;            // Тип пользователя (UT_...)
    public byte Mode;
    public int FirstMessageID;

    public Integer Priority;




    @Override
    public String toString() {
        return this.NickName;
    }

}
