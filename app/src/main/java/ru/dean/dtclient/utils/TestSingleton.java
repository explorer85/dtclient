package ru.dean.dtclient.utils;

/**
 * Created by explorer on 18.10.2014.
 */
public class TestSingleton {
    private static TestSingleton INSTANCE;




    public static synchronized TestSingleton Inst() {
        if(INSTANCE == null) {
            Utils.appendLog("NEWINSTANCETestSingleton!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            INSTANCE = new TestSingleton();
        }
        return INSTANCE;
    }
    private TestSingleton() {

    }



}
