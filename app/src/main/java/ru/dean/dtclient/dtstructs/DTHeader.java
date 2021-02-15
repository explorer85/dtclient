package ru.dean.dtclient.dtstructs;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class DTHeader implements Serializable {
    public short Length;
    public byte Command;
    public byte Flags;
    public int Tale;
    public int Seans;
    public int Tick;
    public long Sender;
    public long Reciever;
    public long Autor;



    public byte[] getData() {

        ByteBuffer bb = ByteBuffer.allocateDirect(40);

        //меняем порядок байт с java на c++ (чтобы понял сервер)
        bb.order( ByteOrder.LITTLE_ENDIAN);

        bb.putShort(Length);
        bb.put(Command);
        bb.put(Flags);
        bb.putInt(Tale);
        bb.putInt(Seans);
        bb.putInt(Tick);
        bb.putLong(Sender);
        bb.putLong(Reciever);
        bb.putLong(Autor);



        byte[] bytes = new byte[40];

        bytes = bb.array();
        return  bytes;
    }

    public void setData(byte[] bytes) {

        ByteBuffer bb = ByteBuffer.allocate(40);
        //меняем порядок байт с java на c++ (чтобы понял сервер)
        bb.order( ByteOrder.LITTLE_ENDIAN);

        bb.put(bytes, 0, 40);

        Length = bb.getShort(0);
        Command = bb.get(2);
        Flags = bb.get(3);
        Tale = bb.getInt(4);
        Seans = bb.getInt(8);
        Tick = bb.getInt(12);
        Sender = bb.getLong(16);
        Reciever = bb.getLong(24);
        Autor = bb.getLong(32);


    }
}
