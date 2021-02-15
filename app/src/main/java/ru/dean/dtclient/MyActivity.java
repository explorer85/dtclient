package ru.dean.dtclient;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.acra.ACRA;

import java.io.File;

import ru.dean.dtclient.dtstructs.Consts;
import ru.dean.dtclient.utils.DTConnectionService;
import ru.dean.dtclient.utils.TestSingleton;
import ru.dean.dtclient.utils.Utils;
import ru.dean.dtclient.views.ChatFragment;
import ru.dean.dtclient.views.ConferenceListFragment;
import ru.dean.dtclient.views.ContactListFragment;
import ru.dean.dtclient.views.LastDialogsFragment;
import ru.dean.dtclient.views.LoginFragment;
import ru.dean.dtclient.views.SettingsActivity;


public class MyActivity extends Activity implements  LoginFragment.OnFragmentInteractionListener, ContactListFragment.OnFragmentInteractionListener,
        ChatFragment.OnFragmentInteractionListener, LastDialogsFragment.OnFragmentInteractionListener, ConferenceListFragment.OnFragmentInteractionListener, ActionBar.TabListener,
        DTConnection.DTConnectionListener  {

    LoginFragment fraglogin;
    public ContactListFragment fragcontactlist;
    public ConferenceListFragment fragconflist;


    Intent serviceintent;
    ServiceConnection sConn;
    DTConnectionService dtService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        //генерируеться ошибка с помощью акра
        //ACRA.getErrorReporter().handleException(null);

        DTConnection.Inst().addListener(this);
        TestSingleton.Inst();

        Utils.appendLog("MyActivity.onCreate");

        //создаем меню бар

        ActionBar bar = getActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ActionBar.Tab tab = bar.newTab();
        tab.setIcon(R.drawable.tab_users);
        tab.setTabListener(this);
        bar.addTab(tab, false);
        tab = bar.newTab();
        tab.setIcon(R.drawable.tab_chat);
        tab.setTabListener(this);
        bar.addTab(tab, false);
        tab = bar.newTab();
        tab.setIcon(R.drawable.tab_conf);
        tab.setTabListener(this);
        bar.addTab(tab, false);








        if (Storage.Inst().user.UID == 0) {

            //очищаем стек фрагментов если при первом запуске процесса в нем что то есть
            if (getFragmentManager().findFragmentByTag("CL") != null) {
                getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }

            fraglogin = new LoginFragment();
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, fraglogin)
                    .commit();
            Utils.appendLog("MyActivity.new LoginFragment()");

        } else {

            //если создаем активити впервый раз
            if (savedInstanceState == null) {
                bar.setSelectedNavigationItem(0);
            }

            //запуск обработки уведомления
            onNewIntent(getIntent());
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("current_tab", getActionBar().getSelectedNavigationIndex());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int current_tab = savedInstanceState.getInt("current_tab");
        getActionBar().setSelectedNavigationItem(current_tab);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Long uid = intent.getLongExtra("UID", 0);
        if (uid != 0) {
            getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            //выбираем таб с последними разговорами
            getActionBar().setSelectedNavigationItem(1);

            ChatFragment fragchat = new ChatFragment();
            Bundle args = new Bundle();
            args.putLong("UID", uid);
            fragchat.setArguments(args);

            getFragmentManager().beginTransaction()
                    .addToBackStack("LAST")
                    .replace(R.id.container, fragchat, "CHAT")
                    .commit();
        }
    }

    @Override
    public void emitSignal(String message, Object obj) {
        if (message == Consts.USERCONNECTED || message == Consts.USERDISCONNECTED) {
            changeConnectionLabel();
        }
    }

    public void changeConnectionLabel() {

        if (DTConnection.Inst().connectionState == 1)
            setTitle("Подключен");
        if (DTConnection.Inst().connectionState == 0)
            setTitle("Переподключение " + DTConnection.Inst().countReconnect);
    }



    @Override
    public void onDestroy()
    {
        Utils.appendLog("MyActivity.onDestroy");
        super.onDestroy();

        DTConnection.Inst().removeListener(this);
        TestSingleton.Inst();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MyActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    public void onBackPressed() {
        super.onBackPressed();
      //  NotificationUtils.Inst(this).deleteAllNotifications();
       // android.os.Process.killProcess(android.os.Process.myPid());

    }



    public void onFragmentInteraction(String message, Object obj) {
        if (message == "1") {


            getActionBar().show();
            getActionBar().setSelectedNavigationItem(0);

        }
    }



    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {

        if (tab.getPosition() == 0) {

            if (getFragmentManager().findFragmentByTag("LAST") != null) {
                getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
                fragcontactlist = new ContactListFragment();
                ft.replace(R.id.container, fragcontactlist, "CL");

        }
        if (tab.getPosition() == 1) {
            if (getFragmentManager().findFragmentByTag("LAST") == null) {
                LastDialogsFragment fraglastdialogs = new LastDialogsFragment();
                ft.replace(R.id.container, fraglastdialogs, "LAST");
            }

        }
        if (tab.getPosition() == 2) {
            if (getFragmentManager().findFragmentByTag("LAST") != null) {
                getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
            fragconflist = new ConferenceListFragment();
            ft.replace(R.id.container, fragconflist, "CONF");
        }
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
        //если выбрана вкладка с диалогами и открыт чат и мы еще раз нажали на таб с диалогами то показываем снова список диалогов
        if (tab.getPosition() == 1) {
            if (getFragmentManager().findFragmentByTag("CHAT") != null) {
                getFragmentManager().popBackStack();
            }

        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != 0) {
            if (requestCode == 1) {

                File file = Storage.Inst().file_path;
                String s = file.getAbsolutePath();
                Bitmap bMap = BitmapFactory.decodeFile(file.getAbsolutePath());
                int a = 5;
                a = 3;

                // ImageView IV = (ImageView) findViewById(R.id."image view");
                //  IV.setImageBitmap(bMap);


            }

            if (requestCode == 2) {

            }
        }
    }


}


//////////////////////////////////////////TODO

//красивый интерфейс   верхнее меню  (там все переделать надо нафик с фрагментами)


//файлы (отсылка фото с камеры)
//публикация в гугол play
//контекстное меню на списке контактов()
//рефакторинг

///////////////////////////////////////////ВЫПОЛНЕНО

//история сообщений с пулл то рефреш

//уведомления о входящих сообщениях
//автологин
//красивый интерфейс   список контактов


/////////////////////////////////////////////МОЖЕТ БЫТЬ
//сделать сервис для поддержания соединения с сервером
//красивый интерфейс   логин
//красивый интерфейс   чат




//    class testTimerTask extends TimerTask {
//
//        @Override
//        public void run() {
//
//            Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
//            // Vibrate for 500 milliseconds
//            v.vibrate(500);
//
//        }
//    };








//стартуем сервис и подключаемся к нему
//"ru.dean.dtclient.utils.DTConnectionService"
//        serviceintent = new Intent(this, DTConnectionService.class);
//        sConn = new ServiceConnection() {
//            public void onServiceConnected(ComponentName name, IBinder binder) {
//                dtService = ((DTConnectionService.MyBinder) binder).getService();
//                Log.d("", "MainActivity onServiceConnected");
//                dtService.s = "1111";
//            }
//            public void onServiceDisconnected(ComponentName name) {
//                Log.d("", "MainActivity onServiceDisconnected");
//            }
//        };




//  startService(serviceintent);
//  boolean c =   bindService(serviceintent, sConn, BIND_AUTO_CREATE);
// unbindService(sConn);










//        if (message == "1") {
//            AlertDialog.Builder builder = new AlertDialog.Builder(MyActivity.this);
//            builder.setTitle("Важное сообщение!")
//                    .setMessage("Покормите кота!")
//                    .setIcon(R.drawable.ic_launcher)
//                    .setCancelable(false)
//                    .setNegativeButton("ОК, иду на кухню",
//                            new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int id) {
//                                    dialog.cancel();
//                                }
//                            });
//            AlertDialog alert = builder.create();
//            alert.show();
//        }

