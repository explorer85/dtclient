package ru.dean.dtclient.threads;

import android.os.Handler;
import android.os.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import ru.dean.dtclient.DTConnection;
import ru.dean.dtclient.utils.Utils;

/**
 * Created by explorer on 11.09.2014.
 */
public class DTSocketThread implements Runnable {
    private static final int SERVERPORT = 5050;
    // private static final String SERVER_IP = "127.0.0.1";
    //private static final String SERVER_IP = "10.0.3.2";
     private static final String SERVER_IP = "77.247.188.78";

    public Socket client;
    public Thread thread;
    private Handler h;
    public String state = "";

    DataOutputStream dataOutputStream= null;
    DataInputStream dataInputStream = null;


    public DTSocketThread(Handler _h) {
        System.out.println("DTSocket started");

        h = _h;
        thread = new Thread(this, "SocketThread");
        thread.start(); // Запускаем поток
    }
    public void connect() {

        state = "login";

    }

    public void disconnect()  {
        try {
            if (DTConnection.Inst().connectionState == -1) {
                state = "";
            }
            else {
                state = "relogin";
            }
        client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        Utils.appendLog("DTSocketThread THREAD STARTED");

        int count_reconnect = 0;

        while (!Thread.currentThread().isInterrupted()) {
            try {

                if (state == "login") {
                    Utils.appendLog("DTSocketThread.login");
                    try {
                        InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                        client = new Socket(serverAddr, SERVERPORT);
                        dataOutputStream = new DataOutputStream(client.getOutputStream());
                        dataInputStream = new DataInputStream(client.getInputStream());

                    } catch (Exception e) {
                        client = null;
                        h.sendEmptyMessage(11);
                        state = "";
                        continue;


                    }
                    state = "work";

                    h.sendEmptyMessage(12);
                }

                if (state == "work") {
                    Utils.appendLog("DTSocketThread.work");

                    byte[] buf = new byte[1024];
                    int r;

                    r = dataInputStream.read(buf);
                    Utils.appendLog("DTSocketThread.work Read complete");
                    if (r > 0) {

                        Message m = h.obtainMessage(0, buf);
                        h.sendMessage(m);
                    } else {
                        Utils.appendLog("DTSocketThread.work ErrorFromServer  relogin");
                        state = "relogin";
                    }

                }


                if (state == "relogin") {
                    try {
                        Utils.appendLog("DTSocketThread.relogin " + String.valueOf(count_reconnect));

                        //h.sendEmptyMessage(14);
                        Message m = h.obtainMessage(14, String.valueOf(count_reconnect));
                        h.sendMessage(m);

                         Thread.sleep(5000);

                        count_reconnect++;


                        InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                        client = new Socket(serverAddr, SERVERPORT);
                        dataOutputStream = new DataOutputStream(client.getOutputStream());
                        dataInputStream = new DataInputStream(client.getInputStream());

                    } catch (Exception e) {
                        client = null;
                        continue;


                    }
                    state = "work";
                    h.sendEmptyMessage(12);
                }


            } catch (IOException e) {
                e.printStackTrace();
                if (state == "work") {
                    state = "relogin";
                }
            }
        }





        Utils.appendLog("DTSocketThread THREAD STOPPED");

    }


    public void writeData(byte[] bytes) {
        try {
            dataOutputStream.write(bytes);
        } catch (IOException e) {

        }
    }







}

//out = soc.getOutputStream();
//        byte buf[] = ("/d").getBytes();
//        out.write(buf);

